package com.arbr.relational_prompting.layers.object_translation

import com.arbr.relational_prompting.generics.model.ChatMessage

data class ExampleTemplateElements(
    val exampleInputElement: List<ChatMessage>,
    val exampleOutputElement: List<ChatMessage>,
)