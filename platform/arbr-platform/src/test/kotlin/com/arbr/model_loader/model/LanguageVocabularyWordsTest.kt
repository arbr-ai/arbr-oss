package com.arbr.model_loader.model

import com.arbr.content_formats.mapper.Mappers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LanguageVocabularyWordsTest {

    private val mapper = Mappers.mapper

    @Test
    fun `serializes to lines`() {
        val vocabularyWords = LanguageVocabularyWords(
            listOf("cat", "dog", "banana")
        )
        val stringValue = mapper.writeValueAsString(vocabularyWords)
        val expectedValue = "{\"content\":\"cat\\ndog\\nbanana\"}"
        Assertions.assertEquals(expectedValue, stringValue)
    }

    @Test
    fun `converts from map`() {
        val vocabularyWords = LanguageVocabularyWords(
            listOf("cat", "dog", "banana")
        )
        val contentMap = vocabularyWords.toJson()

        val converted = mapper.convertValue(contentMap, LanguageVocabularyWords::class.java)
        Assertions.assertTrue(converted.words.isNotEmpty())
        Assertions.assertEquals(3, converted.words.size)
    }

}