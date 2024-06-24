package com.arbr.engine.util

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class FluxPool<T: Any, E: MutableCollection<T>> private constructor(
    private val collection: E,
    private val innerFlux: Flux<T>,
    private val maxBufferSize: Int = 128,
) {

    private fun pooledFlux(): Flux<List<T>> {
        return innerFlux
            .concatMap { elt ->
                Mono.fromCallable {
                    synchronized(collection) {
                        collection.add(elt)
                    }
                }
            }
            .onBackpressureBuffer(maxBufferSize)
            .onBackpressureLatest()
            .concatMap {
                val elements = mutableListOf<T>()
                Mono.empty<Void>()
                    .doOnRequest {
                        synchronized(collection) {
                            elements.addAll(collection.toList())
                            collection.clear()
                        }
                    }
                    .thenReturn(elements)
            }
            .flatMap {
                if (it.isEmpty()) {
                    Mono.empty()
                } else {
                    Mono.just(it)
                }
            }
    }

    companion object {

        fun <T: Any, E: MutableCollection<T>> create(
            initialCollection: E,
            innerFlux: Flux<T>,
        ): Flux<List<T>> {
            return FluxPool(initialCollection, innerFlux).pooledFlux()
        }
    }
}