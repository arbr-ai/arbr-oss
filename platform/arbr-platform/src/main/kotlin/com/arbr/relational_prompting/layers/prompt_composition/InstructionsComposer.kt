package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.layers.object_translation.TemplateElements

fun interface InstructionsComposer {

    fun compose(templateElements: TemplateElements): List<ChatMessage>

    companion object {
        val default = InstructionsComposer { elts ->
            val instructions = (
                    "You are an assistant who, given an input of the following form:\n"
                            + elts.descriptionElements.inputDescriptionElement.description
                            + "\n\nProduces an output of the following form:\n"
                            + elts.descriptionElements.outputDescriptionElement.description
                    )

            listOf(
                ChatMessage(ChatMessage.Role.SYSTEM, instructions),
            )
        }
    }
}