package com.arbr.engine.services.db.client

import com.arbr.db.public.tables.pojos.ApplicationCompletion
import com.arbr.db.public.tables.records.ApplicationCompletionRecord
import com.arbr.db.public.tables.references.APPLICATION_COMPLETION
import com.arbr.engine.services.db.model.WorkflowRawConsumption
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.DSL.value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ApplicationCompletionStore(
    private val dslContext: DSLContext,
) {
    fun insert(
        applicationCompletion: ApplicationCompletionRecord,
    ): Mono<Void> {
        return Mono.from(
            dslContext
                .insertInto(
                    APPLICATION_COMPLETION,
                    APPLICATION_COMPLETION.CACHE_KEY,
                    APPLICATION_COMPLETION.CREATION_TIMESTAMP,
                    APPLICATION_COMPLETION.APPLICATION_ID,
                    APPLICATION_COMPLETION.EXAMPLE_VECTOR_IDS,
                    APPLICATION_COMPLETION.INPUT_VECTOR_IDS,
                    APPLICATION_COMPLETION.INPUT_RESOURCE,
                    APPLICATION_COMPLETION.OUTPUT_RESOURCE,
                    APPLICATION_COMPLETION.PROMPT_MESSAGES,
                    APPLICATION_COMPLETION.COMPLETION_MESSAGES,
                    APPLICATION_COMPLETION.WORKFLOW_ID,
                    APPLICATION_COMPLETION.USED_MODEL,
                    APPLICATION_COMPLETION.PROMPT_TOKENS,
                    APPLICATION_COMPLETION.COMPLETION_TOKENS,
                    APPLICATION_COMPLETION.TOTAL_TOKENS,
                )
                .values(
                    value(applicationCompletion.cacheKey),
                    value(applicationCompletion.creationTimestamp),
                    value(applicationCompletion.applicationId),
                    value(applicationCompletion.exampleVectorIds),
                    value(applicationCompletion.inputVectorIds),
                    value(applicationCompletion.inputResource),
                    value(applicationCompletion.outputResource),
                    value(applicationCompletion.promptMessages),
                    value(applicationCompletion.completionMessages),
                    value(applicationCompletion.workflowId),
                    value(applicationCompletion.usedModel),
                    value(applicationCompletion.promptTokens),
                    value(applicationCompletion.completionTokens),
                    value(applicationCompletion.totalTokens),
                )
                .onConflict()
                .doUpdate()
                .set(applicationCompletion)
        ).then()
    }

    fun get(
        cacheKey: String,
    ): Mono<ApplicationCompletion> {
        return Flux.from(
            dslContext
                .select(
                    APPLICATION_COMPLETION.CACHE_KEY,
                    APPLICATION_COMPLETION.CREATION_TIMESTAMP,
                    APPLICATION_COMPLETION.APPLICATION_ID,
                    APPLICATION_COMPLETION.EXAMPLE_VECTOR_IDS,
                    APPLICATION_COMPLETION.INPUT_VECTOR_IDS,
                    APPLICATION_COMPLETION.INPUT_RESOURCE,
                    APPLICATION_COMPLETION.OUTPUT_RESOURCE,
                    APPLICATION_COMPLETION.PROMPT_MESSAGES,
                    APPLICATION_COMPLETION.COMPLETION_MESSAGES,
                    APPLICATION_COMPLETION.WORKFLOW_ID,
                    APPLICATION_COMPLETION.USED_MODEL,
                    APPLICATION_COMPLETION.PROMPT_TOKENS,
                    APPLICATION_COMPLETION.COMPLETION_TOKENS,
                    APPLICATION_COMPLETION.TOTAL_TOKENS,
                )
                .from(APPLICATION_COMPLETION)
                .where(APPLICATION_COMPLETION.CACHE_KEY.eq(value(cacheKey)))
        )
            .next()
            .map { record: Record ->
                record.into(ApplicationCompletion::class.java)
            }
    }

    fun getTotalUsageByWorkflowHandleId(
        workflowIds: List<Long>,
    ): Mono<List<WorkflowRawConsumption>> {
        return Flux.from(
            dslContext
                .select(
                    APPLICATION_COMPLETION.WORKFLOW_ID,
                    APPLICATION_COMPLETION.USED_MODEL,
                    DSL.sum(APPLICATION_COMPLETION.TOTAL_TOKENS)
                )
                .from(APPLICATION_COMPLETION)
                .where(
                    APPLICATION_COMPLETION.WORKFLOW_ID.`in`(
                        workflowIds.map { value(it) }
                    )
                )
                .groupBy(
                    APPLICATION_COMPLETION.WORKFLOW_ID,
                    APPLICATION_COMPLETION.USED_MODEL,
                )
        )
            .map { (workflowId, usedModel, totalTokens) ->
                WorkflowRawConsumption(
                    workflowId!!,
                    usedModel ?: "unknown",
                    totalTokens.toLong(),
                )
            }
            .collectList()
    }
}
