package com.arbr.og.object_model.common.model

import com.arbr.platform.object_graph.util.AtomicMonoValue
import com.arbr.platform.object_graph.util.AtomicValue
import com.arbr.util.invariants.Invariants
import com.fasterxml.jackson.annotation.JsonValue
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class ProposedValueStream<S : Any>(
    override val identifier: PropertyIdentifier,
    initialValue: S? = null,
) : ProposedValueReadStream<S>, ProposedValueWriteStream<S> {
    private data class MonoWithHandle<T : Any>(
        val id: String,
        val mono: Mono<T>,
    )

    // Sequence of pending (unresolved) values
    private val asyncPendingValueSequence = ConcurrentLinkedDeque<MonoWithHandle<S>>()
    private val proposalProcessingLockedMono = AtomicMonoValue<Proposal<S>>()

    private var latestValueFluxSink: FluxSink<Optional<S>>? = null
    private val latestValueFlux: Flux<Optional<S>> = Flux.create { sink ->
        latestValueFluxSink = sink
        sink.next(Optional.ofNullable(latestValue))
    }
        .share()

    private var latestAcceptedValueFluxSink: FluxSink<Optional<S>>? = null
    private val latestAcceptedValueFlux: Flux<Optional<S>> = Flux.create { sink ->
        latestAcceptedValueFluxSink = sink
        sink.next(Optional.ofNullable(latestAcceptedValue))
    }
        .share()

    /**
     * Tail tracker
     */
    private var latestValue: S? = null

    /**
     * Head tracker
     */
    private var latestAcceptedValue: S? = null

    init {
        if (initialValue != null) {
            latestValue = initialValue
            latestAcceptedValue = initialValue
        }
    }

    /**
     * Add an async proposal. Async proposals are the ground truth latest (future) values.
     * Empty = no update
     */
    @Synchronized
    override fun proposeAsync(update: (S?) -> Mono<S>): Mono<S> {
        val lastAsyncValue: Mono<S> = asyncPendingValueSequence.peekLast()?.mono ?: Mono.justOrEmpty(latestValue)

        val newMonoHandle = UUID.randomUUID().toString()
        val newestValueMono = Mono.defer {
            lastAsyncValue
                .materialize()
                .flatMap { oldValueSignal ->
                    if (oldValueSignal.isOnNext) {
                        val oldValue = oldValueSignal.get()!!

                        // Revert to previous value on failure
                        update(oldValue)
                            .single()
                            .doOnError {
                                // Remove empty/error values since they have no value to arbitrate
                                asyncPendingValueSequence.removeIf { mwh -> mwh.id == newMonoHandle }
                            }
                            .onErrorReturn(oldValue)
                    } else {
                        update(null)
                            .single()
                            .doOnError {
                                // Remove empty/error values since they have no value to arbitrate
                                asyncPendingValueSequence.removeIf { mwh -> mwh.id == newMonoHandle }
                            }
                            .onErrorResume { Mono.empty() }
                    }
                }
        }
            .materialize()
            .doOnNext {
                // Emit synchronous values
                val nullableValue = it.get()
                val optionalValue = Optional.ofNullable(nullableValue)
                latestValue = nullableValue
                latestValueFluxSink?.next(optionalValue)
            }
            .dematerialize<S>()
            .cache()

        // Register async values
        asyncPendingValueSequence.add(MonoWithHandle(newMonoHandle, newestValueMono))

        return newestValueMono
    }

    @Synchronized
    override fun hasAnyUnresolvedProposals(): Boolean {
        return asyncPendingValueSequence.isNotEmpty()
    }

    /**
     * Unsafe inner call to process proposals
     * Requires asyncPendingValues nonempty
     */
    private fun getCombinedBatchProposalInner(
        asyncPendingValues: List<MonoWithHandle<S>>,
        lock: AtomicValue.Lock,
    ): Mono<Proposal<S>> {
        // Note the value emitted by this Mono may not be new if the latest Mono gave empty or error
        // Consumers should be resilient to occasional no-op updates
        val nextUnresolvedAsyncValue = asyncPendingValues.last().mono
        return nextUnresolvedAsyncValue.map { proposedValue ->
            ProposalImpl(
                getLatestAcceptedValue(),
                proposedValue,
                {
                    // Update values before releasing lock
                    synchronized(this) {
                        latestAcceptedValue = proposedValue
                        asyncPendingValueSequence.clear()
                    }

                    // Release lock
                    val didReset = try {
                        lock.release()
                        true
                    } catch (e: AtomicValue.InvalidLockException) {
                        false
                    }

                    // We should always be able to reset the lock we acquired
                    Invariants.check { require ->
                        require(didReset)
                    }

                    latestAcceptedValueFluxSink?.next(Optional.of(proposedValue))
                },
                {
                    // Update values before releasing lock
                    synchronized(this) {
                        // Clear pending values but don't bump accepted
                        asyncPendingValueSequence.clear()
                    }

                    // Reset lock
                    val didReset = try {
                        lock.release()
                        true
                    } catch (e: AtomicValue.InvalidLockException) {
                        false
                    }

                    // We should always be able to reset the lock we acquired
                    Invariants.check { require ->
                        require(didReset)
                    }
                },
            )
        }
    }

    /**
     * Get all unresolved proposals as a combined proposal for batch evaluation.
     */
    @Synchronized
    override fun getCombinedBatchProposal(): Mono<Proposal<S>>? {
        val asyncPendingValues = asyncPendingValueSequence.toList()
        if (asyncPendingValues.isEmpty()) {
            return null
        }

        // Acquire lock
        return proposalProcessingLockedMono.getOrCreate { lock ->
            getCombinedBatchProposalInner(asyncPendingValues, lock)
        }
    }

    @Synchronized
    override fun getLatestAcceptedValue(): S? = latestAcceptedValue

    @Synchronized
    override fun getLatestAcceptedValueFlux(): Flux<Optional<S>> = Flux.concat(
        Flux.just(Optional.ofNullable(getLatestAcceptedValue())),
        latestAcceptedValueFlux,
    )

    @Synchronized
    override fun getLatestValue(): S? = latestValue

    @Synchronized
    override fun getLatestValueFlux(): Flux<Optional<S>> = Flux.concat(
        Flux.just(Optional.ofNullable(getLatestValue())),
        latestValueFlux,
    )

    @JsonValue
    fun toJson() = getLatestValue()

    companion object {
        private val logger = LoggerFactory.getLogger(ProposedValueStream::class.java)
    }
}