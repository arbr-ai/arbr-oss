package com.arbr.relational_prompting.services.ai_application.application

fun interface PromptShortener<I> {

    class ShorteningException: Exception("Cannot shorten prompt any more")

    fun shorten(
        value: I,
        tokenOverage: Int,
    ): I

    companion object {
        fun <I> default(): PromptShortener<I> = PromptShortener { _, _ ->
            throw ShorteningException()
        }
    }
}
