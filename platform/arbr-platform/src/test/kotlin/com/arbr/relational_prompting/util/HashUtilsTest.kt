package com.arbr.relational_prompting.util

import com.arbr.util_common.hashing.HashUtils
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionRequest
import org.junit.jupiter.api.Test

class HashUtilsTest {

    @Test
    fun testCacheKey() {
        val cacheKey = HashUtils.exampleFreeCacheKey(
            "application-id",
            OpenAiChatCompletionRequest.create(
                model = OpenAiChatCompletionModel.GPT_4_0613,
                messages = emptyList(),
                functions = null,
                functionCall = null,
                temperature = null,
                topP = 0.2,
                n = null,
                stream = null,
                stop = null,
                maxTokens = 1024, // Should this use `maxTokens`?
                presencePenalty = null,
                frequencyPenalty = null,
                logitBias = null,
                user = null,
            ),
        )
        println(cacheKey)
    }
}