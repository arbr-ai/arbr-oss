package com.arbr.util_common.reactor

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.*

fun <T> nonBlocking(f: () -> T): Mono<T> = Mono
    .fromCallable(f)
    .subscribeOn(Schedulers.boundedElastic())

fun <T> Mono<T>.fireAndForget(
    scheduler: Scheduler,
): Mono<Void> = Mono.deferContextual { contextView ->
    cache()
        .contextWrite(contextView)
        .subscribeOn(scheduler)
        .subscribe()
        .toMono()
}.then()

class SingleException(message: String) : Exception("No element from single: $message")

fun <T> Mono<T>.single(message: String): Mono<T> = single()
    .onErrorMap(NoSuchElementException::class.java) {
        SingleException(message)
    }

fun <T> Flux<T>.single(message: String): Mono<T> = single()
    .onErrorMap(NoSuchElementException::class.java) {
        SingleException(message)
    }

/**
 * Transform elements of a flux, folding over an initial element and emitting each result.
 */
fun <A, T, U> Flux<T>.concatFold(initial: A, prefetch: Int, transform: (A, T) -> Mono<Pair<A, U>>): Flux<U> {
    var a = initial
    return concatMap({ elt ->
        transform(a, elt).map { (a2, u) ->
            a = a2
            u
        }
    }, prefetch)
}

/**
 * Transform elements of a flux, folding over an initial element and emitting each result.
 */
fun <A, T, U> Flux<T>.concatFold(initial: A, transform: (A, T) -> Mono<Pair<A, U>>): Flux<U> {
    var a = initial
    return concatMap { elt ->
        transform(a, elt).map { (a2, u) ->
            a = a2
            u
        }
    }
}

/**
 * Switch if empty and explicitly propagate context
 */
fun <T> Mono<T>.switchIfEmptyContextual(s: () -> Mono<T>): Mono<T> = this.switchIfEmpty(nonBlocking(s).flatMap { it })

fun <T: Any> Mono<T>.andPeriodically(
    duration: Duration,
    performOperation: (Long) -> Unit,
): Mono<T> {
    val periodicFlux = Flux.interval(duration, duration)
        .doOnNext(performOperation)
        .map { Optional.empty<T>() }

    return this
        .map { Optional.of(it) }
        .mergeWith(periodicFlux)
        .flatMap {
            if (it.isPresent) {
                Mono.just(it.get())
            } else {
                Mono.empty()
            }
        }
        .next()
}
