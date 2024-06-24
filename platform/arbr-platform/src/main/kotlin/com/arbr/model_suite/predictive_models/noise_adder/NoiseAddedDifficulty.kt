package com.arbr.model_suite.predictive_models.noise_adder

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import kotlin.math.pow

enum class NoiseAddedDifficulty(@JsonValue val serializedName: String) {
    EASY("easy"),
    NORMAL("normal"),
    HARD("hard");

    @JsonIgnore
    fun multiplier(): Double {
        return when (this) {
            EASY -> 0.5
            NORMAL -> 1.0
            HARD -> 2.0
        }
    }

    /**
     * Scale probability by the entropy multiplier associated with the difficulty level
     * Note near zero this is very similar to multiplier * probability
     */
    fun scaleProbability(probability: Double): Double {
        return 1 - (1 - probability).pow(multiplier())
    }
}