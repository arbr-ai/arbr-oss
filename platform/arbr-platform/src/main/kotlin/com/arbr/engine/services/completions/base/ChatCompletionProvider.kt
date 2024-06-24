package com.arbr.engine.services.completions.base

import com.arbr.relational_prompting.generics.model.OpenAiChatCompletion
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionRequest
import reactor.core.publisher.Mono

interface ChatCompletionProvider {

    fun getChatCompletion(
        applicationId: String,
        request: OpenAiChatCompletionRequest,
    ): Mono<OpenAiChatCompletion>
}
