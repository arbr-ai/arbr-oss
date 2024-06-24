package com.arbr.engine.services.user

import com.arbr.content_formats.jsonb.serializeToJsonb
import com.arbr.content_formats.mapper.Mappers
import com.arbr.db.public.tables.pojos.UserProjectWorkflowResource
import com.arbr.db.public.tables.references.USER_PROJECT_WORKFLOW_RESOURCE
import com.arbr.util_common.reactor.single
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface WorkflowResourceRepository {
    fun getValid(
        workflowId: Long
    ): Flux<UserProjectWorkflowResource>

    fun get(
        objectModelUuid: String,
    ): Mono<UserProjectWorkflowResource>

    fun getWorkflowResources(
        workflowId: Long,
    ): Flux<UserProjectWorkflowResource>

    fun upsertResource(
        workflowId: Long,
        objectModelUuid: String,
        parentObjectModelUuid: String?,
        resourceType: String,
        resourceData: Map<String, Any>,
    ): Mono<UserProjectWorkflowResource>

    fun updateResource(
        objectModelUuid: String,
        resourceData: Map<String, Any>
    ): Mono<Void>
}

class WorkflowResourceRepositoryFactory(
    private val dslContext: DSLContext,
) {

    fun makeResourceRepository(): WorkflowResourceRepository {
        return WorkflowResourceRepositoryImpl(dslContext)
    }
}

class WorkflowResourceRepositoryImpl(
    private val dslContext: DSLContext,
) : WorkflowResourceRepository {
    private val mapper = Mappers.mapper

    override fun getValid(
        workflowId: Long
    ): Flux<UserProjectWorkflowResource> {
        return Flux.from(
            dslContext
                .select(
                    USER_PROJECT_WORKFLOW_RESOURCE.ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID,
                    USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_TYPE,
                    USER_PROJECT_WORKFLOW_RESOURCE.PARENT_RESOURCE_ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA,
                    USER_PROJECT_WORKFLOW_RESOURCE.ORDINAL,
                    USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID,
                )
                .from(USER_PROJECT_WORKFLOW_RESOURCE)
                .where(
                    USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID.eq(DSL.value(workflowId)),
                    USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID.eq(DSL.value(true)),
                )
                .orderBy(
                    USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP
                )
        )
            .map { record ->
                record.into(UserProjectWorkflowResource::class.java)
            }
    }

    override fun get(
        objectModelUuid: String,
    ): Mono<UserProjectWorkflowResource> {
        return Mono.from(
            dslContext
                .select(
                    USER_PROJECT_WORKFLOW_RESOURCE.ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID,
                    USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_TYPE,
                    USER_PROJECT_WORKFLOW_RESOURCE.PARENT_RESOURCE_ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA,
                    USER_PROJECT_WORKFLOW_RESOURCE.ORDINAL,
                    USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID,
                )
                .from(USER_PROJECT_WORKFLOW_RESOURCE)
                .where(
                    USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID.eq(DSL.value(objectModelUuid)),
                )
        )
            .map { record ->
                record.into(UserProjectWorkflowResource::class.java)
            }
    }

    override fun getWorkflowResources(
        workflowId: Long,
    ): Flux<UserProjectWorkflowResource> {
        return Flux.from(
            dslContext
                .select(
                    USER_PROJECT_WORKFLOW_RESOURCE.ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID,
                    USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_TYPE,
                    USER_PROJECT_WORKFLOW_RESOURCE.PARENT_RESOURCE_ID,
                    USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA,
                    USER_PROJECT_WORKFLOW_RESOURCE.ORDINAL,
                    USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID,
                )
                .from(USER_PROJECT_WORKFLOW_RESOURCE)
                .where(
                    USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID.eq(DSL.value(workflowId)),
                )
                .orderBy(USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP)
        )
            .map { record ->
                record.into(UserProjectWorkflowResource::class.java)
            }
    }

    override fun upsertResource(
        workflowId: Long,
        objectModelUuid: String,
        parentObjectModelUuid: String?,
        resourceType: String,
        resourceData: Map<String, Any>,
    ): Mono<UserProjectWorkflowResource> {
        val resourceDataJsonb = serializeToJsonb(mapper, resourceData)

        val creationTimestamp = Instant.now().toEpochMilli()

        val parentResourceIdMono = if (parentObjectModelUuid == null) {
            Mono.just(Optional.empty())
        } else {
            get(parentObjectModelUuid).map { Optional.of(it) }
                .single("No parent matching $parentObjectModelUuid")
                .onErrorResume {
                    logger.warn("No parent matching {} - possible orphan resource", parentObjectModelUuid)
                    Mono.just(Optional.empty())
                }
        }

        return parentResourceIdMono.flatMap { parentOpt ->
            val parentId = parentOpt.getOrNull()?.id

            Mono.from(
                dslContext
                    .insertInto(
                        USER_PROJECT_WORKFLOW_RESOURCE,
                        USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID,
                        USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP,
                        USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP,
                        USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID,
                        USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_TYPE,
                        USER_PROJECT_WORKFLOW_RESOURCE.PARENT_RESOURCE_ID,
                        USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA,
                        USER_PROJECT_WORKFLOW_RESOURCE.ORDINAL,
                        USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID,
                    )
                    .values(
                        DSL.value(objectModelUuid),
                        DSL.value(creationTimestamp),
                        DSL.value(creationTimestamp),
                        DSL.value(workflowId),
                        DSL.value(resourceType),
                        DSL.value(parentId),
                        DSL.value(resourceDataJsonb),
                        DSL.value(0),
                        DSL.value(true),
                    )
                    .onConflict(
                        USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID
                    )
                    .doUpdate()
                    .set(USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP, DSL.value(creationTimestamp))
                    // .set(USER_PROJECT_WORKFLOW_RESOURCE.PARENT_RESOURCE_ID, DSL.value(parentId))
                    .set(USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA, DSL.value(resourceDataJsonb))
                    .set(USER_PROJECT_WORKFLOW_RESOURCE.ORDINAL, DSL.value(0))
                    .set(USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID, DSL.value(true))
                    .returning(
                        USER_PROJECT_WORKFLOW_RESOURCE.ID,
                        USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID,
                        USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP,
                        USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP,
                        USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID,
                        USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_TYPE,
                        USER_PROJECT_WORKFLOW_RESOURCE.PARENT_RESOURCE_ID,
                        USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA,
                        USER_PROJECT_WORKFLOW_RESOURCE.ORDINAL,
                        USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID,
                    )
            )
                .map { record ->
                    record.into(UserProjectWorkflowResource::class.java)
                }
        }
    }

    override fun updateResource(
        objectModelUuid: String,
        resourceData: Map<String, Any>
    ): Mono<Void> {
        val updatedTimestamp = Instant.now().toEpochMilli()

        return Mono.from(
            dslContext
                .update(USER_PROJECT_WORKFLOW_RESOURCE)
                .set(USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA, serializeToJsonb(mapper, resourceData))
                .set(USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP, updatedTimestamp)
                .where(USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID.eq(DSL.value(objectModelUuid)))
        ).then()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowResourceRepository::class.java)
    }

}
