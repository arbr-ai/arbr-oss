package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.relational_prompting.generics.model.ChatMessage

fun interface ChatMessagePreProcessor {

    fun process(chatMessage: ChatMessage): ChatMessage

    companion object {
        val default = ChatMessagePreProcessor { it }
    }
}
