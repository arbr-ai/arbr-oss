package com.arbr.model_suite.predictive_models.noise_adder

import com.arbr.content_formats.format.tokenizer.LinearPlaintextTokenizer

fun interface DataPointParser<T: Any> {
    fun parse(value: String): T

    companion object {
        fun <T: Any> ofTokenizer(
            tokenizer: LinearPlaintextTokenizer<T>
        ) = DataPointParser<List<T>> { text ->
            tokenizer.tokenize(text)
        }
    }
}