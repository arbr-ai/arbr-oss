package com.arbr.api_server_base.service.user

import com.arbr.db.public.tables.pojos.UserProjectWorkflowEventStream
import com.arbr.db.public.tables.references.USER_PROJECT_WORKFLOW_EVENT_STREAM
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class WorkflowEventStreamRepository(
    private val dslContext: DSLContext
) {

    fun insert(
        workflowId: Long,
        topicName: String,
        stateHash: String,
        ordinalOffset: Long,
        eventTimestamp: Long
    ): Mono<UserProjectWorkflowEventStream> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_PROJECT_WORKFLOW_EVENT_STREAM,
                    USER_PROJECT_WORKFLOW_EVENT_STREAM.WORKFLOW_ID,
                    USER_PROJECT_WORKFLOW_EVENT_STREAM.TOPIC_NAME,
                    USER_PROJECT_WORKFLOW_EVENT_STREAM.STATE_HASH,
                    USER_PROJECT_WORKFLOW_EVENT_STREAM.ORDINAL_OFFSET,
                    USER_PROJECT_WORKFLOW_EVENT_STREAM.EVENT_TIMESTAMP
                )
                .values(
                    DSL.value(workflowId),
                    DSL.value(topicName),
                    DSL.value(stateHash),
                    DSL.value(ordinalOffset),
                    DSL.value(eventTimestamp)
                )
                .returning()
        )
            .map { record ->
                record.into(UserProjectWorkflowEventStream::class.java)
            }
    }

    fun getById(id: Long): Mono<UserProjectWorkflowEventStream> {
        return Mono.from(
            dslContext
                .selectFrom(USER_PROJECT_WORKFLOW_EVENT_STREAM)
                .where(USER_PROJECT_WORKFLOW_EVENT_STREAM.ID.eq(DSL.value(id)))
        )
            .map { record ->
                record.into(UserProjectWorkflowEventStream::class.java)
            }
    }

    fun getByWorkflowId(workflowId: Long): Flux<UserProjectWorkflowEventStream> {
        return Flux.from(
            dslContext
                .selectFrom(USER_PROJECT_WORKFLOW_EVENT_STREAM)
                .where(USER_PROJECT_WORKFLOW_EVENT_STREAM.WORKFLOW_ID.eq(DSL.value(workflowId)))
        )
            .map { record ->
                record.into(UserProjectWorkflowEventStream::class.java)
            }
    }

    fun getByTopicName(topicName: String): Flux<UserProjectWorkflowEventStream> {
        return Flux.from(
            dslContext
                .selectFrom(USER_PROJECT_WORKFLOW_EVENT_STREAM)
                .where(USER_PROJECT_WORKFLOW_EVENT_STREAM.TOPIC_NAME.eq(DSL.value(topicName)))
        )
            .map { record ->
                record.into(UserProjectWorkflowEventStream::class.java)
            }
    }

    fun getByStateHash(stateHash: String): Flux<UserProjectWorkflowEventStream> {
        return Flux.from(
            dslContext
                .selectFrom(USER_PROJECT_WORKFLOW_EVENT_STREAM)
                .where(USER_PROJECT_WORKFLOW_EVENT_STREAM.STATE_HASH.eq(DSL.value(stateHash)))
        )
            .map { record ->
                record.into(UserProjectWorkflowEventStream::class.java)
            }
    }

    fun getByOrdinalOffset(ordinalOffset: Long): Flux<UserProjectWorkflowEventStream> {
        return Flux.from(
            dslContext
                .selectFrom(USER_PROJECT_WORKFLOW_EVENT_STREAM)
                .where(USER_PROJECT_WORKFLOW_EVENT_STREAM.ORDINAL_OFFSET.eq(DSL.value(ordinalOffset)))
        )
            .map { record ->
                record.into(UserProjectWorkflowEventStream::class.java)
            }
    }
}
