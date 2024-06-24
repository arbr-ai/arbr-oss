package com.arbr.relational_prompting.services.ai_application.application

import com.arbr.content_formats.mapper.Mappers
import com.arbr.content_formats.tokens.TokenizationUtils
import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInboundEdge
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.generics.model.CompletionUsageData
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletion
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionRequest
import com.arbr.relational_prompting.services.ai_application.config.AiApplicationConfig
import com.arbr.relational_prompting.services.ai_application.config.OutputFormatException
import com.arbr.relational_prompting.services.ai_application.config.OutputSchemaExceptionWithKnownViolations
import com.arbr.relational_prompting.services.ai_application.model.TypedApplicationCompletion
import com.arbr.relational_prompting.services.embedding.model.TemplateElementLiteral
import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair
import com.arbr.util_common.hashing.HashUtils
import com.arbr.util_common.reactor.switchIfEmptyContextual
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class PromptExpectedTooLongException(aiApplicationId: String, tokenLength: Int) :
    Exception("Prompt for $aiApplicationId expected to be too long at $tokenLength")

class CompletionLengthLimitedException(val completionTokens: Int) :
    Exception("Completion length limited even with buffer")

open class AiApplication<InputModel : SourcedStruct, OutputModel : SourcedStruct>(
    private val applicationCompletionCache: ApplicationCompletionCache,
    private val applicationExampleProvider: ApplicationExampleProvider,
    private val chatCompletionProvider: ChatCompletionProvider,
    val config: AiApplicationConfig<InputModel, OutputModel>,
) {
    val applicationId: String = config.applicationId
    private val numExamplesToIncludeInPrompt: Long = config.numExamplesToIncludeInPrompt

    /**
     * Publish the input/output pair as an example for the application and return the created pairs.
     *
     * NOTE:
     * Here we take literal output messages as an argument to support custom output parsers, since otherwise each
     * output parser would also need to specify a serializer for examples to match the custom form.
     */
    private fun publishExample(
        input: InputModel,
        output: OutputModel,
        outputMessages: List<ChatMessage>,
    ): Flux<EmbeddedResourcePair> {
        val inputMessages = config.promptComposer.composeInput(
            config.inputSchema,
            config.outputSchema,
            emptyList(),
            input,
        )

        val inputLiteral = TemplateElementLiteral(
            config.inputSchema,
            input,
            inputMessages,
        )
        val outputLiteral = TemplateElementLiteral(
            config.outputSchema,
            output,
            outputMessages,
        )

        return applicationExampleProvider.publish(
            inputLiteral,
            outputLiteral,
        )
    }

    private fun getExamples(arg: InputModel): Mono<List<VectorResourceKeyValuePair<InputModel, OutputModel>>> {
        return Flux.fromIterable(config.hardcodedExamples).concatMap {
            hydrateExample(it.first, it.second)
        }.collectList().flatMap { hardCodedExamplesList ->
            if (hardCodedExamplesList.size >= numExamplesToIncludeInPrompt) {
                Mono.just(hardCodedExamplesList)
            } else {
                applicationExampleProvider.retrieveNearestNeighbors(
                    config.inputSchema,
                    config.outputSchema,
                    arg,
                    numExamplesToIncludeInPrompt * 10, // Hack
                )
                    .take(numExamplesToIncludeInPrompt - hardCodedExamplesList.size)
                    .collectList()
                    .map {
                        hardCodedExamplesList + it
                    }
            }
        }
    }

    private fun exampleFreeCacheKey(request: OpenAiChatCompletionRequest): String {
        // Hack: reduce messages in request to first and last, assuming all others are from examples.
        val modifiedRequest = request.withMessages(
            request.messages.take(1) + request.messages.takeLast(1)
        )

        return exampleFreeCacheKey(
            applicationId,
            modifiedRequest,
        )
    }

    private fun composeMessageListIntoRequest(
        arg: InputModel,
        examples: List<VectorResourceKeyValuePair<InputModel, OutputModel>>,
        targetCompletionTokens: Int,
        trailingChatMessages: List<ChatMessage>,
    ): Pair<List<VectorResourceKeyValuePair<InputModel, OutputModel>>, OpenAiChatCompletionRequest> {
        val promotableModels = config.promotableModels.toMutableList()
        val functionTokenCount = config.completionRequest.functions
            ?.let { TokenizationUtils.getTokenCount(mapper.writeValueAsString(it)) } ?: 0
        val deduplicatedExamples = examples.distinctBy { it.vectorId }

        var thisArg = arg
        var model = config.completionRequest.model
        var dropI = 0

        // When set on an iteration, check that the token count has strictly decreased.
        var checkTokensDecreasedFrom: Int? = null

        while (true) {
            val truncatedExamples = deduplicatedExamples.dropLast(dropI)

            // Delegate to the prompt composer
            val result = config.promptComposer.compose(
                config.inputSchema,
                config.outputSchema,
                truncatedExamples.map { it.key to it.value },
                thisArg,
            ).messages + trailingChatMessages

            val tokenEstimate = if (result.isEmpty()) {
                0
            } else {
                TokenizationUtils.getTokenCount(result.joinToString("\n") { it.content })
            } + targetCompletionTokens + functionTokenCount + tokenMargin * result.size

            checkTokensDecreasedFrom?.let { previousTokenEstimate ->
                if (previousTokenEstimate <= tokenEstimate) {
                    logger.warn("Prompt shortener of $applicationId failed to shorten")
                    throw PromptExpectedTooLongException(applicationId, tokenEstimate)
                }

                logger.info("Successfully shortened prompt from $previousTokenEstimate to $tokenEstimate estimated tokens")
                checkTokensDecreasedFrom = null
            }

            if (tokenEstimate <= model.tokenLimit) {
                // Add remaining buffer to max tokens
                return truncatedExamples to config.completionRequest.withMessages(result).withModel(model)
                    .withMaxTokens(targetCompletionTokens)
            } else if (truncatedExamples.isEmpty() && promotableModels.isEmpty()) {
                try {
                    thisArg = config.promptShortener.shorten(arg, tokenEstimate - model.tokenLimit)
                    checkTokensDecreasedFrom = tokenEstimate
                    logger.info("Attempted to shorten argument")
                } catch (e: PromptShortener.ShorteningException) {
                    // No remaining examples, no promotable models, and prompt not shortenable - fail.
                    throw PromptExpectedTooLongException(applicationId, tokenEstimate)
                }
            } else if (truncatedExamples.isEmpty()) {
                // No remaining examples but there is a promotable model
                model = promotableModels.removeAt(0)
                dropI = 0
                logger.warn("Promoting $applicationId to $model due to prompt length of $tokenEstimate.")
            } else {
                // There are remaining examples
                dropI++
            }
        }
    }

    private fun hydrateExample(
        inputModel: InputModel,
        outputModel: OutputModel,
    ): Mono<VectorResourceKeyValuePair<InputModel, OutputModel>> {
        // Hydrate the example by publishing it
        val exampleMessages = config.promptComposer.composeExamples(
            config.inputSchema,
            config.outputSchema,
            listOf(inputModel to outputModel),
        ).first()

        val exampleInputMessages = exampleMessages.exampleInputElement
        val exampleOutputMessages = exampleMessages.exampleOutputElement

        val inputLiteral = TemplateElementLiteral(
            config.inputSchema,
            inputModel,
            exampleInputMessages,
        )
        val outputLiteral = TemplateElementLiteral(
            config.outputSchema,
            outputModel,
            exampleOutputMessages,
        )

        return applicationExampleProvider.publish(
            inputLiteral,
            outputLiteral,
        )
            .collectList()
            .map {
                val erp = it.first()
                val vectorId = erp.vectorId!!

                VectorResourceKeyValuePair(
                    vectorId,
                    inputLiteral,
                    outputLiteral,
                )
            }
    }

    private fun parseChatCompletion(
        arg: InputModel,
        chatCompletion: OpenAiChatCompletion,
        cacheKey: String,
    ): Mono<Pair<OutputModel, CompletionUsageData>> {
        val firstChoice = chatCompletion
            .choices
            .firstOrNull()
        return if (firstChoice == null) {
            Mono.error(Exception("No message in chat completion"))
        } else if (firstChoice.finishReason == OpenAiChatCompletion.FinishReason.LENGTH) {
            Mono.error(CompletionLengthLimitedException(chatCompletion.usage.completionTokens))
        } else {
            val message = firstChoice.message

            val parentIds = arg.values().map { it.id }
            val generatorInfo = SourcedValueGeneratorInfo(
                listOf(
                    SourcedValueGeneratorInboundEdge(
                        applicationId = applicationId,
                        completionCacheKey = cacheKey,
                        operationId = null,
                        parentValueIds = parentIds,
                    )
                )
            )

            try {
                Mono.justOrEmpty(
                    config.outputSchema.parse(
                        config.chatMessagePreProcessor.process(message),
                        generatorInfo
                    )?.let {
                        it to CompletionUsageData(
                            chatCompletion.model,
                            chatCompletion.usage.promptTokens,
                            chatCompletion.usage.completionTokens,
                            chatCompletion.usage.totalTokens,
                        )
                    }
                )
            } catch (e: Exception) {
                Mono.error(e)
            }
        }
    }

    private data class ChatCompletionResult<M>(
        val chatCompletion: OpenAiChatCompletion,
        val outputModel: M,
        val usageData: CompletionUsageData,
    )

    private fun handleOutputSchemaViolationException(
        arg: InputModel,
        schemaViolationExceptionWithKnownViolations: OutputSchemaExceptionWithKnownViolations,
        maxCompletionTokens: Int,
    ): Mono<ChatCompletionResult<OutputModel>> {
        if (!config.attemptOutputSchemaRepair) {
            return Mono.error(schemaViolationExceptionWithKnownViolations)
        }

        val newMessages =
            listOf(
                ChatMessage(
                    ChatMessage.Role.ASSISTANT,
                    schemaViolationExceptionWithKnownViolations.messageContent,
                ),
                ChatMessage(
                    ChatMessage.Role.USER,
                    config.outputSchemaRepairFormatter.process(schemaViolationExceptionWithKnownViolations.violations),
                )
            )

        // Compose a new request without any examples to make room for the new schema violation messages
        // Note: some duplication of code here. Consider defining a nested application for repair instead.
        val (_, newRequest) = composeMessageListIntoRequest(
            arg,
            emptyList(),
            maxCompletionTokens,
            newMessages,
        )

        val cacheKey = exampleFreeCacheKey(newRequest)
        val cmplMono = chatCompletionProvider.getChatCompletion(applicationId, newRequest)

        return cmplMono.flatMap { chatCompletion ->
            parseChatCompletion(arg, chatCompletion, cacheKey).map { (outputModel, usageData) ->
                ChatCompletionResult(chatCompletion, outputModel, usageData)
            }
        }
    }

    private fun attemptGetChatCompletion(
        arg: InputModel,
        examples: List<VectorResourceKeyValuePair<InputModel, OutputModel>>,
        artifactSink: FluxSink<ApplicationArtifact>,
        maxCompletionTokens: Int,
    ): Mono<OutputModel> {
        // Right now the cache key takes examples into consideration, which means repeat runs will likely
        // cache miss due to new examples
        val (usedExamples, request) = try {
            composeMessageListIntoRequest(arg, examples, maxCompletionTokens, emptyList())
        } catch (e: Exception) {
            return Mono.error(e)
        }
        val messages = request.messages

        val cacheKey = exampleFreeCacheKey(request)

        return applicationCompletionCache
            .get(
                applicationId,
                config.inputSchema,
                config.outputSchema,
                arg,
                cacheKey,
            )
            .map { typedApplicationCompletion ->
                logger.info(
                    "Got cached completion for $applicationId: ${
                        mapper.writeValueAsString(
                            config.outputSchema.serializedToSourcedValues(
                                typedApplicationCompletion.outputResource
                            )
                        )
                    }"
                )
                typedApplicationCompletion
            }
            .map { typedApplicationCompletion ->
                artifactSink.next(
                    ApplicationArtifact(
                        applicationId,
                        usedExamples,
                        arg,
                        typedApplicationCompletion.outputResource,
                        emptyList(),
                        typedApplicationCompletion.usedModel,
                        typedApplicationCompletion.promptTokens,
                        typedApplicationCompletion.completionTokens,
                        typedApplicationCompletion.totalTokens,
                    )
                )
                typedApplicationCompletion.outputResource
            }
            .switchIfEmptyContextual {
                val cmplMono = chatCompletionProvider.getChatCompletion(applicationId, request)

                Mono.defer {
                    cmplMono
                        .flatMap { chatCompletion ->
                            parseChatCompletion(arg, chatCompletion, cacheKey)
                                .map { (outputModel, usageData) ->
                                    ChatCompletionResult(chatCompletion, outputModel, usageData)
                                }
                        }
                        .onErrorResume(OutputSchemaExceptionWithKnownViolations::class.java) { schemaException ->
                            handleOutputSchemaViolationException(
                                arg,
                                schemaException,
                                maxCompletionTokens,
                            )
                        }
                }
                    .retryWhen(
                        Retry.max(config.numOutputFormatRetries.toLong())
                            .filter {
                                it is OutputFormatException
                            }
                            .doBeforeRetry {
                                logger.info("Retrying on output format exception: ${this.applicationId}")
                            }
                    )
                    .flatMap { chatCompletionResult ->
                        val outputModel = chatCompletionResult.outputModel
                        val usageData = chatCompletionResult.usageData
                        val completionMessages =
                            listOfNotNull(chatCompletionResult.chatCompletion.choices.firstOrNull()?.message)

                        publishExample(
                            input = arg,
                            output = outputModel,
                            outputMessages = completionMessages,
                        )
                            .collectList()
                            .doOnNext { pairs ->
                                val vectorIds = pairs.mapNotNull { it.vectorId }
                                artifactSink.next(
                                    ApplicationArtifact(
                                        applicationId,
                                        usedExamples,
                                        arg,
                                        outputModel,
                                        vectorIds,
                                        usageData.usedModel,
                                        usageData.promptTokens.toLong(),
                                        usageData.completionTokens.toLong(),
                                        usageData.totalTokens.toLong(),
                                    )
                                )
                            }
                            .flatMap { pairs ->
                                val vectorIds = pairs.mapNotNull { it.vectorId }
                                applicationCompletionCache.set(
                                    applicationId,
                                    config.inputSchema,
                                    config.outputSchema,
                                    TypedApplicationCompletion(
                                        cacheKey,
                                        Instant.now().toEpochMilli(),
                                        applicationId,
                                        usedExamples.map { it.vectorId },
                                        vectorIds,
                                        arg,
                                        outputModel,
                                        messages,
                                        completionMessages,
                                        usageData.usedModel,
                                        usageData.promptTokens.toLong(),
                                        usageData.completionTokens.toLong(),
                                        usageData.totalTokens.toLong(),
                                    )
                                )
                            }
                            .thenReturn(outputModel)
                    }
            }
    }

    private fun nextMaxCompletionTokens(
        maxCompletionTokens: Int
    ): Int {
        return if (maxCompletionTokens == 0) {
            1
        } else {
            2 * maxCompletionTokens.takeHighestOneBit()
        }
    }

    private fun getChatCompletion(
        arg: InputModel,
        examples: List<VectorResourceKeyValuePair<InputModel, OutputModel>>,
        artifactSink: FluxSink<ApplicationArtifact>
    ): Mono<OutputModel> {
        // Start by composing the prompt to accommodate the configured number of completion tokens. If the
        // response from OpenAI indicates that more tokens are needed (CompletionLengthLimitedException), bump
        // this number and try again.
        val maxCompletionTokens =
            AtomicInteger(config.completionRequest.maxTokens ?: assumedMaxTokensConfiguredIfNull)

        return Mono.defer {
            attemptGetChatCompletion(
                arg,
                examples,
                artifactSink,
                maxCompletionTokens.get(),
            )
        }
            .retryWhen(
                Retry.max(config.numCompletionLengthRetries.toLong())
                    .filter {
                        it is CompletionLengthLimitedException
                    }
                    .doBeforeRetry {
                        val oldCompletionTokens = maxCompletionTokens.get()
                        val completionTokens =
                            (it.failure()!! as CompletionLengthLimitedException).completionTokens
                        val nextCompletionTokens = nextMaxCompletionTokens(completionTokens)
                        maxCompletionTokens.set(nextCompletionTokens)
                        if (nextCompletionTokens <= oldCompletionTokens) {
                            throw Exception("Impossible: new token requirement not more than previous")
                        }
                        logger.info("Retrying on length-limited exception ($nextCompletionTokens > $completionTokens > $oldCompletionTokens): ${this.applicationId}")
                    }
            )
    }

    fun invoke(arg: InputModel, artifactSink: FluxSink<ApplicationArtifact>): Mono<OutputModel> {
        return getExamples(arg).flatMap { examples ->
            getChatCompletion(arg, examples, artifactSink)
        }
    }

    companion object {
        const val ALLOW_CACHED_COMPLETIONS = "allow_cached_completions"

        private const val assumedMaxTokensConfiguredIfNull = 1024

        private val logger = LoggerFactory.getLogger(AiApplication::class.java)

        /**
         * Margin of error for token count
         */
        private const val tokenMargin = 16

        private const val cacheKeyPrefix = "v4"

        private val mapper = Mappers
            .mapper
            .copy()
            .configure(
                SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true
            )

        private fun exampleFreeCacheKey(applicationId: String, request: OpenAiChatCompletionRequest): String {
            // Hack: reduce messages in request to first and last, assuming all others are from examples.
            val modifiedRequest = request.withMessages(
                request.messages.take(1) + request.messages.takeLast(1)
            )

            val mapRepresentation = mapOf(
                "application_id" to applicationId,
                "request" to modifiedRequest,
            )

            val md = MessageDigest.getInstance("SHA-1")
            md.update("$cacheKeyPrefix:".toByteArray())

            val requestMap: Map<String, Any> = mapper.convertValue(mapRepresentation, jacksonTypeRef())
            HashUtils.hashObject(md, "completion", requestMap)

            val bytes = md.digest()
            val no = BigInteger(1, bytes)
            return no.toString(16)
        }
    }
}
