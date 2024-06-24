package com.arbr.platform.object_graph.util

import com.arbr.util.invariants.Invariants
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink

internal class AtomicMonoValue<T: Any> {

    private val atomicValue = AtomicValue<Mono<T>>()

    private var mono: Mono<T>? = null
    private var monoSink: MonoSink<T>? = null

    @Synchronized
    fun getOrCreate(create: (AtomicValue.Lock) -> Mono<T>): Mono<T> {
        val (lock, wrappedMono) = atomicValue.getOrSet {
            Mono.create { sink ->
                monoSink = sink
            }
        }

        val newOrExistingMono = if (lock == null) {
            Invariants.check { require ->
                require(mono != null)
            }

            mono ?: Mono.error(IllegalStateException())
        } else {
            val innerMono = create(lock)
                .doOnEach {
                    if (it.isOnNext) {
                        monoSink?.success(it.get())
                    } else if (it.isOnComplete) {
                        monoSink?.success()
                    } else {
                        monoSink?.error(it.throwable!!)
                    }
                }

            Mono.defer {
                wrappedMono.zipWith(innerMono).map { it.t1 }
            }.cache()
        }

        mono = newOrExistingMono

        return newOrExistingMono
    }

}