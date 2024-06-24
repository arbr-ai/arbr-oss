package com.arbr.kafka.differential_models

import com.arbr.kafka.topic.base.ApiKafkaConsumerEvent
import com.arbr.kafka.topic.base.ApiKafkaTopicNodeFactory
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("MemberVisibilityCanBePrivate")
abstract class DifferentialModelSink<K, S, D>(
    private val nodeFactory: ApiKafkaTopicNodeFactory,
    private val binding: DifferentialModelBinding<K, S, D>,
) {
    private val initialState: DifferentialModelTrackedState<S> = binding.initialState
    private val splitter: DifferentialModelSplitter<S, D> = binding.splitter
    private val reducer: DifferentialModelReducer<S, D> = binding.reducer

    /**
     * Retrieve the consumer node of differential updates.
     */
    private val consumerNode = nodeFactory.consumerNode<K, D>(binding.diffClass)
        ?: run {
            logger.warn("Failed to create consumer node for sink ${this::class.java.simpleName}")

            throw NullPointerException()
        }

    protected abstract fun ingestEvent(
        event: ApiKafkaConsumerEvent<K, D>,
    ): Mono<Void>

    abstract fun getLatestTrackedState(): Mono<DifferentialModelTrackedState<S>>

    protected abstract fun getOffset(
        hash: String
    ): Mono<Long>

    protected abstract fun getState(
        hash: String
    ): Mono<DifferentialModelTrackedState<S>>

    /**
     * Useful client-facing API
     */

    private fun groupId(channel: DifferentialModelUpdateChannel): String? {
        return when (channel) {
            DifferentialModelUpdateChannel.INGESTION -> null // Consume each update once
            DifferentialModelUpdateChannel.DISPLAY -> "display-${UUID.randomUUID()}" // Replay updates per display
        }
    }

    /**
     * Fold the reduce operator over each element and emit each result.
     */
    private fun <T, U> foldReduce(
        flux: Flux<T>,
        reduce: (U?, T) -> U,
    ): Flux<U> {
        val operands = ConcurrentHashMap<Long, U>()
        return flux
            .index()
            .map { (index, t) ->
                val operand = operands.remove(index - 1)
                reduce(operand, t)
                    .also { operands[index] = it }
            }
    }

    private fun getUpdatesFromCheckpointState(
        topicKey: K,
        trackedState: DifferentialModelTrackedState<S>,
        offset: Long?,
        channel: DifferentialModelUpdateChannel,
    ): Flux<DifferentialModelTrackedDiffApplication<D>> {
        val groupId = groupId(channel)

        val splitStateDiffs = splitter.split(trackedState.model)
        val trackedDiffLedger = reducer.reconstructLedger(initialState, splitStateDiffs)

        val replayFlux =
            foldReduce<DifferentialModelTrackedDiffApplication<D>, DifferentialModelTrackedStateDiffApplication<S, D>>(
                Flux.fromIterable(trackedDiffLedger)
            ) { runningTrackedStateDiff, diffApplication ->
                val diff = diffApplication.diff
                val priorState = runningTrackedStateDiff?.priorTrackedState ?: initialState
                val nextState = reducer.reduce(priorState, diff)

                DifferentialModelTrackedStateDiffApplication(
                    priorState,
                    diff,
                    nextState,
                )
            }

        val newEvents: Flux<ApiKafkaConsumerEvent<K, D>> = consumerNode
            .receive(
                topicKey,
                offset,
                groupId,
            )
            .flatMap {
                ingestEvent(it)
                    .materialize()
                    .thenReturn(it)
            }

        val newDifferentialFlux =
            foldReduce<ApiKafkaConsumerEvent<K, D>, DifferentialModelTrackedStateDiffApplication<S, D>>(newEvents) { runningTrackedStateDiff, event ->
                val diff = event.messageObject.objectValue
                val priorState =
                    runningTrackedStateDiff?.priorTrackedState ?: trackedState // Begin at the checkpoint tracked state
                val nextState = reducer.reduce(priorState, diff)

                DifferentialModelTrackedStateDiffApplication(
                    priorState,
                    diff,
                    nextState,
                )
            }

        return Flux.concat(
            replayFlux,
            newDifferentialFlux,
        ).map { trackedStateDiffApplication ->
            DifferentialModelTrackedDiffApplication(
                trackedStateDiffApplication.priorTrackedState.hash,
                trackedStateDiffApplication.diff,
                trackedStateDiffApplication.nextTrackedState.hash,
            )
        }
    }

    fun getUpdatesAfterHash(
        topicKey: K,
        hash: String,
        channel: DifferentialModelUpdateChannel,
    ): Flux<DifferentialModelTrackedDiffApplication<D>> {
        return Mono.zip(
            getOffset(hash),
            getState(hash)
        )
            .flatMapMany { (offset, state) ->
                getUpdatesFromCheckpointState(
                    topicKey,
                    state,
                    offset,
                    channel
                )
            }
            .switchIfEmpty(
                Flux.defer {
                    getUpdatesFromCheckpointState(
                        topicKey = topicKey,
                        trackedState = initialState,
                        offset = null,
                        channel = channel,
                    )
                }
            )
    }

    fun getNewestUpdates(
        topicKey: K,
        channel: DifferentialModelUpdateChannel,
    ): Flux<DifferentialModelTrackedDiffApplication<D>> {
        return getLatestTrackedState()
            .flatMapMany { state ->
                getOffset(state.hash).flatMapMany { offset ->
                    getUpdatesFromCheckpointState(
                        topicKey,
                        state,
                        offset,
                        channel
                    )
                }
                    .switchIfEmpty(
                        Flux.defer {
                            getUpdatesFromCheckpointState(
                                topicKey = topicKey,
                                trackedState = state,
                                offset = null,
                                channel = channel
                            )
                        }
                    )
            }
            .switchIfEmpty(
                Flux.defer {
                    getUpdatesFromCheckpointState(
                        topicKey = topicKey,
                        trackedState = initialState,
                        offset = null,
                        channel = channel
                    )
                }
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DifferentialModelSink::class.java)
    }
}
