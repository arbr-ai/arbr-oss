package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.relational_prompting.layers.object_translation.schema
import com.arbr.relational_prompting.layers.prompt_composition.ChatMessagePreProcessor
import com.arbr.relational_prompting.layers.prompt_composition.ExampleMessageComposer
import com.arbr.relational_prompting.layers.prompt_composition.InputMessageComposer
import com.arbr.relational_prompting.layers.prompt_composition.InstructionsComposer
import com.arbr.relational_prompting.layers.prompt_composition.OutputSchemaRepairFormatter
import com.arbr.relational_prompting.layers.prompt_composition.PromptComposer
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListParser
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListSerializer
import com.arbr.relational_prompting.services.ai_application.application.PromptShortener

interface AiApplicationConfigFactory {
    fun builder(
        applicationId: String,
    ): Builder0

    companion object {
        private const val defaultNumExamplesToIncludeInPrompt = 2L

        private const val defaultMaxTokens = 1024L

        private const val defaultNumOutputFormatRetries = 3
        private const val defaultNumCompletionLengthRetries = 3

        private const val defaultAttemptOutputSchemaRepair = true

        class Builder0(
            private val baseConfig: AiApplicationBaseConfig,
            private val completionModelFilter: CompletionModelFilter,
            private val applicationId: String,
        ) {

            fun <InputModel : SourcedStruct> withInputSchema(
                inputSchema: TemplateComponentSchema<InputModel>
            ) = Builder1(
                baseConfig,
                completionModelFilter,
                applicationId,
                inputSchema,
            )

            inline fun <reified InputModel : SourcedStruct, B : TemplateComponentSchema.BuilderBase<InputModel, B>> withInputSchema(
                f: TemplateComponentSchema.Builder0.() -> B
            ): Builder1<InputModel> = withInputSchema(schema(f))
        }

        class Builder1<InputModel : SourcedStruct>(
            private val baseConfig: AiApplicationBaseConfig,
            private val completionModelFilter: CompletionModelFilter,
            private val applicationId: String,
            private val inputSchema: TemplateComponentSchema<InputModel>,
        ) {
            private var propertyListSerializer: PropertyListSerializer? = null

            fun withInputSerializer(
                propertyListSerializer: PropertyListSerializer
            ): Builder1<InputModel> {
                return also {
                    this.propertyListSerializer = propertyListSerializer
                }
            }

            fun <OutputModel : SourcedStruct> withOutputSchema(outputSchema: TemplateComponentSchema<OutputModel>) =
                Builder2(
                    baseConfig,
                    completionModelFilter,
                    applicationId,
                    propertyListSerializer?.let { pls -> inputSchema.withPropertyListSerializer(pls) } ?: inputSchema,
                    outputSchema,
                )

            inline fun <reified OutputModel : SourcedStruct, B : TemplateComponentSchema.BuilderBase<OutputModel, B>> withOutputSchema(
                f: TemplateComponentSchema.Builder0.() -> B
            ): Builder2<InputModel, OutputModel> = withOutputSchema(schema(f))
        }

        class Builder2<InputModel : SourcedStruct, OutputModel : SourcedStruct>(
            private val baseConfig: AiApplicationBaseConfig,
            private val completionModelFilter: CompletionModelFilter,
            private val applicationId: String,
            private val inputSchema: TemplateComponentSchema<InputModel>,
            private val outputSchema: TemplateComponentSchema<OutputModel>,
        ) {
            private var propertyListParser: PropertyListParser<OutputModel>? = null
            var numExamplesToIncludeInPrompt: Long = defaultNumExamplesToIncludeInPrompt
            var hardcodedExamples: MutableList<Pair<InputModel, OutputModel>> = mutableListOf()
            var promptComposer: PromptComposer? = null
            var instructionsComposer: InstructionsComposer = InstructionsComposer.default
            var exampleMessageComposer: ExampleMessageComposer = ExampleMessageComposer.default
            val inputMessageComposer: InputMessageComposer = InputMessageComposer.default
            var promptShortener: PromptShortener<InputModel> = PromptShortener.default()
            var chatMessagePreProcessor: ChatMessagePreProcessor = ChatMessagePreProcessor.default
            var maxTokens: Long = defaultMaxTokens
            var model: OpenAiChatCompletionModel = OpenAiChatCompletionModel.GPT_3_5_TURBO_0125
            var attemptOutputSchemaRepair = defaultAttemptOutputSchemaRepair
            var outputSchemaRepairFormatter: OutputSchemaRepairFormatter = OutputSchemaRepairFormatter.default

            fun withOutputParser(propertyListParser: PropertyListParser<OutputModel>): Builder2<InputModel, OutputModel> {
                return also {
                    this.propertyListParser = propertyListParser
                }
            }

            fun withInstructions(instr: (String, String) -> String): Builder2<InputModel, OutputModel> {
                instructionsComposer = InstructionsComposer {
                    listOf(
                        ChatMessage(
                            ChatMessage.Role.SYSTEM,
                            instr(
                                it.descriptionElements.inputDescriptionElement.description,
                                it.descriptionElements.outputDescriptionElement.description
                            )
                        )
                    )
                }
                return this
            }

            fun withPromptShortener(promptShortener: PromptShortener<InputModel>): Builder2<InputModel, OutputModel> {
                return also {
                    this.promptShortener = promptShortener
                }
            }

            fun withOutputProcessor(chatMessagePreProcessor: ChatMessagePreProcessor): Builder2<InputModel, OutputModel> {
                return also {
                    this.chatMessagePreProcessor = chatMessagePreProcessor
                }
            }

            fun withModel(model: OpenAiChatCompletionModel): Builder2<InputModel, OutputModel> {
                return if (!completionModelFilter.allow(model)) {
                    // deny
                    this
                } else {
                    also {
                        this.model = model
                    }
                }
            }

            fun withMaxTokens(maxTokens: Long): Builder2<InputModel, OutputModel> {
                return also { this.maxTokens = maxTokens }
            }

            fun withNumExamplesToIncludeInPrompt(numExamplesToIncludeInPrompt: Long): Builder2<InputModel, OutputModel> {
                return also {
                    this.numExamplesToIncludeInPrompt = numExamplesToIncludeInPrompt
                }
            }

            fun withExample(
                inputModel: InputModel,
                outputModel: OutputModel,
            ): Builder2<InputModel, OutputModel> = also {
                hardcodedExamples.add(inputModel to outputModel)
            }

            fun configure(f: Builder2<InputModel, OutputModel>.() -> Unit): Builder2<InputModel, OutputModel> {
                f(this)
                return this
            }

            fun build(): AiApplicationConfig<InputModel, OutputModel> {
                // Default prompt composer combines other utilities specified in the config
                val composer = promptComposer
                    ?: PromptComposer(
                        instructionsComposer,
                        exampleMessageComposer,
                        inputMessageComposer
                    )

                return AiApplicationConfig(
                    inputSchema,
                    propertyListParser?.let { pls -> outputSchema.withPropertyListParser(pls) } ?: outputSchema,
                    applicationId,
                    composer,
                    numExamplesToIncludeInPrompt,
                    hardcodedExamples,
                    promptShortener,
                    chatMessagePreProcessor,
                    maxTokens,
                    baseConfig.topP,
                    model,
                    defaultNumOutputFormatRetries,
                    defaultNumCompletionLengthRetries,
                    attemptOutputSchemaRepair,
                    outputSchemaRepairFormatter,
                )
            }
        }
    }
}
