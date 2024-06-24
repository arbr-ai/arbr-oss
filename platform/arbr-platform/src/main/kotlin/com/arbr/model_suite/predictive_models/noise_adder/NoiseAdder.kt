package com.arbr.model_suite.predictive_models.noise_adder

import reactor.core.publisher.Flux

interface NoiseAdder {
    fun addNoise(
        baseDocument: String,
        patch: String,
    ): Flux<Pair<String, NoiseAddedDifficulty>>
}

