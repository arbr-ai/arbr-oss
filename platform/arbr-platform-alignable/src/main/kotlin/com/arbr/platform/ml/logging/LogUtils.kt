package com.arbr.platform.ml.logging

import reactor.core.publisher.Mono

object LogUtils {

    fun <T> setMDCLossContext(source: LossTracker.Source, loss: Double, monoSupplier: () -> Mono<T>): Mono<T> {
        return Mono.defer {
            monoSupplier()
        }.contextWrite { ctx ->
            val lossTracker = ctx.getOrDefault("training_loss", LossTracker())!!
            lossTracker.set(source, loss)
            ctx.put("training_loss", lossTracker)
        }
    }

}