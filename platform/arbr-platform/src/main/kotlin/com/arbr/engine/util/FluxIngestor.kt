package com.arbr.engine.util

import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ConcurrentLinkedQueue

class FluxIngestor<T> {
    private data class IngestionOperation<T>(
        val operationSink: MonoSink<T>,
        val perform: () -> Mono<T>,
    )

    private val ingestionPreQueue = ConcurrentLinkedQueue<IngestionOperation<T>>()
    private var ingestionSink: FluxSink<IngestionOperation<T>>? = null

    private val ingestionFlux = Flux.create<IngestionOperation<T>> { sink ->
        setIngestionSink(sink)
    }
        .flatMap { operation ->
            operation.perform()
                .materialize()
                .flatMap { signal ->
                    if (signal.isOnNext) {
                        val value = signal.get()!!
                        operation.operationSink.success(value)
                        Mono.just(value)
                    } else if (signal.isOnError) {
                        val error = signal.throwable ?: IllegalStateException()
                        operation.operationSink.error(error)
                        Mono.error(error)
                    } else {
                        operation.operationSink.success()
                        Mono.empty()
                    }
                }
        }

    init {
        ingestionFlux
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    @Synchronized
    private fun setIngestionSink(sink: FluxSink<IngestionOperation<T>>) {
        ingestionSink = sink
        for (preOp in ingestionPreQueue) {
            sink.next(preOp)
        }
    }

    @Synchronized
    private fun enqueue(ingestionOperation: IngestionOperation<T>) {
        val ingestionSink = ingestionSink
        if (ingestionSink == null) {
            ingestionPreQueue.add(ingestionOperation)
        } else {
            ingestionSink.next(ingestionOperation)
        }
    }

    fun ingest(operation: () -> Mono<T>): Mono<T> {
        return Mono.create { sink ->
            val ingestionOperation = IngestionOperation(
                sink,
            ) {
                operation()
            }
            enqueue(ingestionOperation)
        }
    }
}