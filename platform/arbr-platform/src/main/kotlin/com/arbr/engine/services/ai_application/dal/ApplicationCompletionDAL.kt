package com.arbr.engine.services.ai_application.dal

import com.arbr.content_formats.jsonb.serializeToJsonb
import com.arbr.db.public.tables.records.ApplicationCompletionRecord
import com.arbr.engine.services.db.client.ApplicationCompletionStore
import com.arbr.engine.services.workflow.state.WorkflowExecutorService
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.relational_prompting.services.ai_application.application.AiApplication
import com.arbr.relational_prompting.services.ai_application.model.TypedApplicationCompletion
import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.jooq.JSONB
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Component
class ApplicationCompletionDAL(
    private val applicationCompletionStore: ApplicationCompletionStore,
    private val mapper: ObjectMapper,
    @Value("\${topdown.cache.enabled:true}")
    private val cacheEnabled: String,
    private val exampleProvider: ApplicationExampleProvider,
) : ApplicationCompletionCache {
    private val genericSourcedValueList = jacksonTypeRef<List<SourcedValue<*>>>()

    /**
     * Cache required to use nearest example - debug behavior.
     */
    private val cacheForceExamples = cacheEnabled.lowercase() == "force_examples"

    /**
     * Cache all results - normal behavior.
     */
    private val cacheAll = cacheEnabled.lowercase() == "true"

    /**
     * Cache nothing.
     */
    private val cacheNone = cacheEnabled.lowercase() == "false"

    /**
     * Cache values during the runtime of the service (by modifying the cache key)
     */
    private val cacheRuntime = !cacheAll && !cacheNone

    private val cacheKeyPrefix = if (cacheRuntime) UUID.randomUUID().toString().takeLast(4) else ""

    private fun <T> parseJsonb(jsonb: JSONB, clazz: Class<T>): T {
        return mapper.readValue(jsonb.data(), clazz)
    }

    private inline fun <reified T> parseJsonb(jsonb: JSONB): T {
        return parseJsonb(jsonb, T::class.java)
    }

    fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> getFromCache(
        applicationId: String,
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        cacheKey: String,
    ): Mono<TypedApplicationCompletion<InputModel, OutputModel>> = applicationCompletionStore.get(cacheKey)
        .mapNotNull<TypedApplicationCompletion<InputModel, OutputModel>> {
            val inputProxyObject = mapper.readValue(it.inputResource.data(), genericSourcedValueList)
            val outputProxyObject = mapper.readValue(it.outputResource.data(), genericSourcedValueList)

            val inputValue = try {
                inputSchema.deserializeFromSourcedValues(inputProxyObject)
            } catch (e: Exception) {
                logger.warn(
                    "Found cache hit on $applicationId for key $cacheKey but input value was uninterpretable",
                    e
                )
                return@mapNotNull null
            }
            val outputValue = try {
                outputSchema.deserializeFromSourcedValues(outputProxyObject)
            } catch (e: Exception) {
                logger.error(
                    "Found cache hit on $applicationId for key $cacheKey but output value was uninterpretable",
                    e
                )
                return@mapNotNull null
            }

            try {
                mapper.writeValueAsString(inputValue)
            } catch (e: Exception) {
                logger.warn("Unusable input value from cache", e)
                return@mapNotNull null
            }

            try {
                mapper.writeValueAsString(outputValue)
            } catch (e: Exception) {
                logger.warn("Unusable output value from cache", e)
                return@mapNotNull null
            }

            TypedApplicationCompletion(
                it.cacheKey,
                it.creationTimestamp,
                it.applicationId,
                parseJsonb(it.exampleVectorIds),
                parseJsonb(it.inputVectorIds),
                inputValue,
                outputValue,
                parseJsonb(it.promptMessages),
                parseJsonb(it.completionMessages ?: JSONB.jsonb("[]")),
                it.usedModel ?: "",
                it.promptTokens ?: 0,
                it.completionTokens ?: 0,
                it.totalTokens ?: 0,
            )
        }

    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> get(
        applicationId: String,
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        input: InputModel,
        cacheKey: String
    ): Mono<TypedApplicationCompletion<InputModel, OutputModel>> {
        val exampleCacheMono = if (cacheForceExamples) {
            exampleProvider.retrieveNearestNeighbors(
                inputSchema,
                outputSchema,
                input,
                numNeighborsToRetrieve = 1L,
            )
                .collectList()
                .defaultIfEmpty(emptyList())
                .mapNotNull<VectorResourceKeyValuePair<InputModel, OutputModel>> { examples ->
                    examples.firstOrNull()
                }
                .map {
                    TypedApplicationCompletion(
                        "",
                        0L,
                        applicationId,
                        emptyList(),
                        emptyList(),
                        it.key.obj,
                        it.value.obj,
                        it.key.chatMessages,
                        it.value.chatMessages,
                        "",
                        0,
                        0,
                        0,
                    )
                }
        } else {
            Mono.empty()
        }

        return exampleCacheMono.switchIfEmpty {
            if (cacheAll || cacheRuntime) {
                val cacheKeyModified = cacheKeyPrefix + cacheKey

                Mono.deferContextual { contextView ->
                    val allowCache = contextView
                        .getOrDefault<Boolean>(AiApplication.ALLOW_CACHED_COMPLETIONS, true)
                        ?: true

                    if (allowCache) {
                        getFromCache(
                            applicationId,
                            inputSchema,
                            outputSchema,
                            cacheKeyModified,
                        )
                    } else {
                        logger.info("Skipping cache for $cacheKeyModified")
                        Mono.empty()
                    }
                }
            } else {
                Mono.empty()
            }
        }
    }

    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> set(
        applicationId: String,
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        typedApplicationCompletion: TypedApplicationCompletion<InputModel, OutputModel>
    ): Mono<Void> {
        val cacheKeyModified = cacheKeyPrefix + typedApplicationCompletion.cacheKey

        return Mono.deferContextual { ctxView ->
            val workflowHandleId = ctxView.get<String?>(WorkflowExecutorService.WORKFLOW_ID_CONTEXT_KEY).toLongOrNull()

            if (workflowHandleId == null) {
                logger.warn("Workflow handle ID missing for attribution on {}", cacheKeyModified)
            }

            applicationCompletionStore.insert(
                ApplicationCompletionRecord(
                    cacheKey = cacheKeyModified,
                    creationTimestamp = typedApplicationCompletion.creationTimestamp,
                    applicationId = typedApplicationCompletion.applicationId,
                    exampleVectorIds = serializeToJsonb(mapper, typedApplicationCompletion.exampleVectorIds),
                    inputVectorIds = serializeToJsonb(mapper, typedApplicationCompletion.inputVectorIds),
                    inputResource = serializeToJsonb(
                        mapper,
                        inputSchema.serializedToSourcedValues(typedApplicationCompletion.inputResource)
                    ),
                    outputResource = serializeToJsonb(
                        mapper,
                        outputSchema.serializedToSourcedValues(typedApplicationCompletion.outputResource)
                    ),
                    promptMessages = serializeToJsonb(mapper, typedApplicationCompletion.promptMessages),
                    completionMessages = serializeToJsonb(mapper, typedApplicationCompletion.completionMessages),
                    workflowId = workflowHandleId,
                    usedModel = typedApplicationCompletion.usedModel,
                    promptTokens = typedApplicationCompletion.promptTokens,
                    completionTokens = typedApplicationCompletion.completionTokens,
                    totalTokens = typedApplicationCompletion.totalTokens,
                )
            ).then()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApplicationCompletionDAL::class.java)
    }
}