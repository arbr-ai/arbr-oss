package com.arbr.model_suite.predictive_models.noise_adder

import java.util.*

data class DiscreteDist<T : Any>(
    val samples: List<Pair<T, Int>>,
) {
    private val sampleCount = samples.sumOf { it.second }

    fun sample(random: Random): T? {
        if (samples.isEmpty()) {
            return null
        }

        val sampleCdfArg = random.nextInt(sampleCount)
        var cumulativeSum = 0
        for ((t, p) in samples) {
            cumulativeSum += p
            if (sampleCdfArg < cumulativeSum) {
                return t
            }
        }

        throw IllegalStateException()
    }
}