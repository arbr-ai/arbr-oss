package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel

fun interface CompletionModelFilter {
    fun allow(model: OpenAiChatCompletionModel): Boolean

    companion object {
        /**
         * Default - allow all
         */
        fun default() = CompletionModelFilter { true }
    }
}