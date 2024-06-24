package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.layers.object_translation.ExampleTemplateElements
import com.arbr.relational_prompting.layers.object_translation.ExampleTemplateValueElements
import com.arbr.relational_prompting.layers.object_translation.TemplateElements

interface ExampleMessageComposer {

    fun composeExampleTemplateElements(
        exampleValueElements: ExampleTemplateValueElements
    ): ExampleTemplateElements

    fun composeFlat(templateElements: TemplateElements): List<ChatMessage>

    companion object {
        val default = object : ExampleMessageComposer {

            override fun composeExampleTemplateElements(exampleValueElements: ExampleTemplateValueElements): ExampleTemplateElements {
                val inputMessages = listOf(
                    ChatMessage(
                        ChatMessage.Role.USER,
                        exampleValueElements.exampleInputValueElement.serializedValue,
                    )
                )
                val outputMessages = listOf(
                    ChatMessage(
                        ChatMessage.Role.ASSISTANT,
                        exampleValueElements.exampleOutputValueElements.serializedValue,
                    )
                )

                return ExampleTemplateElements(inputMessages, outputMessages)
            }

            override fun composeFlat(templateElements: TemplateElements): List<ChatMessage> {
                return templateElements.exampleValueElements.flatMap { it.exampleInputElement + it.exampleOutputElement }
            }

        }
    }
}