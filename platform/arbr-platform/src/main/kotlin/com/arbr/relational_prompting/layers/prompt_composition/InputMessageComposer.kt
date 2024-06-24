package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.layers.object_translation.TemplateElements

fun interface InputMessageComposer {

    fun compose(templateElements: TemplateElements): List<ChatMessage>

    companion object {
        val default = InputMessageComposer { elts ->
            listOf(
                ChatMessage(ChatMessage.Role.USER, elts.inputValueElement.serializedValue),
            )
        }
    }
}