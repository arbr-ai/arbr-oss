package com.arbr.relational_prompting.layers.object_translation

import com.arbr.relational_prompting.generics.model.ChatMessage

data class ChatPromptTemplate(
    val messages: List<ChatMessage>
)