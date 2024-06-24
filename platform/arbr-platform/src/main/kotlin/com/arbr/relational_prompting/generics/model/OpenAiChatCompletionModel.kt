package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class OpenAiChatCompletionModel(@JsonValue val id: String) {
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k"),
    GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613"),
    GPT_3_5_TURBO_16K_0613("gpt-3.5-turbo-16k-0613"),
    GPT_3_5_TURBO_1106("gpt-3.5-turbo-1106"),
    GPT_3_5_TURBO_0125("gpt-3.5-turbo-0125"),
    GPT_4("gpt-4"),
    GPT_4_0314("gpt-4-0314"),
    GPT_4_0613("gpt-4-0613"),
    GPT_4_1106_PREVIEW("gpt-4-1106-preview"),
    GPT_4_0125_PREVIEW("gpt-4-0125-preview");

    val tokenLimit: Int
        get() = when (this) {
            GPT_3_5_TURBO -> 4096
            GPT_3_5_TURBO_16K -> 16384
            GPT_3_5_TURBO_0613 -> 4096
            GPT_3_5_TURBO_16K_0613 -> 16384
            GPT_3_5_TURBO_1106 -> 16384
            GPT_3_5_TURBO_0125 -> 16384
            GPT_4 -> 8192
            GPT_4_0314 -> 8192
            GPT_4_0613 -> 8192
            GPT_4_1106_PREVIEW -> 8192
            GPT_4_0125_PREVIEW -> 8192
        }

    companion object {
        @JsonCreator
        @JvmStatic
        fun create(
            model: String
        ): OpenAiChatCompletionModel {
            return OpenAiChatCompletionModel.values().first { it.id == model }
        }
    }
}
