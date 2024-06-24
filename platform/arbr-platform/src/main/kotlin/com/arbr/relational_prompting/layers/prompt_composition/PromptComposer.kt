package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.relational_prompting.layers.object_translation.*
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.services.embedding.model.TemplateElementLiteral

class PromptComposer(
    val instructionsComposer: InstructionsComposer,
    val exampleMessageComposer: ExampleMessageComposer,
    val inputMessageComposer: InputMessageComposer,
) {

    private fun <R1 : SourcedStruct, R2 : SourcedStruct> prepareTemplateElements(
        inputSchema: TemplateComponentSchema<R1>,
        outputSchema: TemplateComponentSchema<R2>,
        examplePairs: List<Pair<TemplateElementLiteral<R1>, TemplateElementLiteral<R2>>>,
        inputRecord: R1,
    ): TemplateElements {
        val descriptionElements = TemplateDescriptionElements(
            inputSchema.description(),
            outputSchema.description(),
        )
        val exampleElements = examplePairs.map { (input, output) ->
            ExampleTemplateElements(
                input.chatMessages,
                output.chatMessages,
            )
        }
        val inputElement = inputSchema.serializedValue(inputRecord)

        return TemplateElements(
            descriptionElements,
            exampleElements,
            inputElement,
        )
    }

    fun <R1 : SourcedStruct, R2 : SourcedStruct> composeInput(
        inputSchema: TemplateComponentSchema<R1>,
        outputSchema: TemplateComponentSchema<R2>,
        examplePairs: List<Pair<TemplateElementLiteral<R1>, TemplateElementLiteral<R2>>>,
        inputRecord: R1,
    ): List<ChatMessage> {
        val elements = prepareTemplateElements(
            inputSchema, outputSchema, examplePairs, inputRecord,
        )

        return inputMessageComposer.compose(elements)
    }

    fun <R1 : SourcedStruct, R2 : SourcedStruct> composeExamples(
        inputSchema: TemplateComponentSchema<R1>,
        outputSchema: TemplateComponentSchema<R2>,
        exampleRecordPairs: List<Pair<R1, R2>>,
    ): List<ExampleTemplateElements> {
        val exampleTemplateValueElements = exampleRecordPairs.map { (inputRecord, outputRecord) ->
            ExampleTemplateValueElements(
                inputSchema.serializedValue(inputRecord),
                outputSchema.serializedValue(outputRecord),
            )
        }

        return exampleTemplateValueElements.map(exampleMessageComposer::composeExampleTemplateElements)
    }

    fun <R1 : SourcedStruct, R2 : SourcedStruct> compose(
        inputSchema: TemplateComponentSchema<R1>,
        outputSchema: TemplateComponentSchema<R2>,
        examplePairs: List<Pair<TemplateElementLiteral<R1>, TemplateElementLiteral<R2>>>,
        inputRecord: R1,
    ): ChatPromptTemplate {
        val elements = prepareTemplateElements(
            inputSchema, outputSchema, examplePairs, inputRecord,
        )

        return ChatPromptTemplate(
            instructionsComposer.compose(elements)
                    + exampleMessageComposer.composeFlat(elements)
                    + inputMessageComposer.compose(elements)
        )
    }
}