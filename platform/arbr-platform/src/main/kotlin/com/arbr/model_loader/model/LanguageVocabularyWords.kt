package com.arbr.model_loader.model

import com.arbr.data_common.base.DataRecordObject
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class LanguageVocabularyWords(
    val words: List<String>
): DataRecordObject {
    @JsonValue
    fun toJson(): Map<String, Any?> {
        val joined = words.joinToString("\n")
        return mapOf(
            "content" to joined
        )
    }

    companion object {
        @JsonCreator
        @JvmStatic
        fun create(
            value: Map<String, Any?>,
        ): LanguageVocabularyWords {
            val mapValues = value.values.toList()
            if (mapValues.size != 1) {
                throw IllegalArgumentException()
            }
            val plaintext = mapValues.first().toString()

            val lines = plaintext.split("\n")
            return LanguageVocabularyWords(lines)
        }
    }
}
