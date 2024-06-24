package com.arbr.engine.services.user

import com.arbr.content_formats.jsonb.serializeToJsonb
import com.arbr.content_formats.mapper.Mappers
import com.arbr.db.public.tables.pojos.UserProjectWorkflow
import com.arbr.db.public.tables.records.UserProjectWorkflowRecord
import com.arbr.db.public.tables.references.USER_PROJECT_WORKFLOW
import org.jooq.DSLContext
import org.jooq.impl.DSL
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WorkflowStatusRepository {
    fun insert(
        projectId: Long,
        creationTimestamp: Long,
        workflowType: String,
        status: Int,
        statusRecordedTimestamp: Long,
        plans: List<Any>, // TODO: Deprecate
        commits: List<Any>, // TODO: Deprecate
        requestedUserInputs: List<Any>, // TODO: Deprecate
        valuedUserInputs: List<Any>, // TODO: Deprecate
        buildArtifacts: List<Any>, // TODO: Deprecate
        paramMap: Map<String, String>,
        idempotencyKey: String,
    ): Mono<UserProjectWorkflow>

    /**
     *     id                             bigserial    not null unique,
     *     project_id                     bigint       not null references user_project (id),
     *     creation_timestamp             bigint       not null,
     *     workflow_type                  varchar(511) not null, -- the kind of workflow
     *     last_status                    int          not null, -- the most recently recorded status
     *     last_status_recorded_timestamp bigint       not null,
     *     plan_info                      jsonb        not null,
     *     commit_info                    jsonb        not null,
     *     requested_user_inputs          jsonb        not null,
     *     valued_user_inputs             jsonb        not null,
     *     build_artifacts                jsonb        not null, -- outputs of the build
     *     param_map                      jsonb,                 -- input parameter map
     *     idempotency_key                varchar(511)
     */
    fun update(
        workflowId: Long,
        updateRecord: UserProjectWorkflowRecord,
    ): Mono<UserProjectWorkflow>

    fun get(
        workflowId: Long
    ): Mono<UserProjectWorkflow>

    fun getForProject(
        projectId: Long,
    ): Flux<UserProjectWorkflow>

    fun getForProject(
        projectId: Long,
        workflowTypes: List<String>,
        earliestStatusRecorded: Long,
    ): Flux<UserProjectWorkflow>

    fun getByIdempotencyKey(
        idempotencyKey: String,
    ): Mono<UserProjectWorkflow>
}

class WorkflowStatusRepositoryFactory(
    private val dslContext: DSLContext,
) {

    fun makeStatusRepository(): WorkflowStatusRepository {
        return WorkflowStatusRepositoryImpl(dslContext)
    }
}

class WorkflowStatusRepositoryImpl(
    private val dslContext: DSLContext,
) : WorkflowStatusRepository {
    private val mapper = Mappers.mapper

    override fun insert(
        projectId: Long,
        creationTimestamp: Long,
        workflowType: String,
        status: Int,
        statusRecordedTimestamp: Long,
        plans: List<Any>, // TODO: Deprecate
        commits: List<Any>, // TODO: Deprecate
        requestedUserInputs: List<Any>, // TODO: Deprecate
        valuedUserInputs: List<Any>, // TODO: Deprecate
        buildArtifacts: List<Any>, // TODO: Deprecate
        paramMap: Map<String, String>,
        idempotencyKey: String,
    ): Mono<UserProjectWorkflow> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_PROJECT_WORKFLOW,
                    USER_PROJECT_WORKFLOW.PROJECT_ID,
                    USER_PROJECT_WORKFLOW.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.WORKFLOW_TYPE,
                    USER_PROJECT_WORKFLOW.LAST_STATUS,
                    USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.PLAN_INFO,
                    USER_PROJECT_WORKFLOW.COMMIT_INFO,
                    USER_PROJECT_WORKFLOW.REQUESTED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.VALUED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.BUILD_ARTIFACTS,
                    USER_PROJECT_WORKFLOW.PARAM_MAP,
                    USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY,
                )
                .values(
                    DSL.value(projectId),
                    DSL.value(creationTimestamp),
                    DSL.value(workflowType),
                    DSL.value(status),
                    DSL.value(statusRecordedTimestamp),
                    DSL.value(serializeToJsonb(mapper, plans)),
                    DSL.value(serializeToJsonb(mapper, commits)),
                    DSL.value(serializeToJsonb(mapper, requestedUserInputs)),
                    DSL.value(serializeToJsonb(mapper, valuedUserInputs)),
                    DSL.value(serializeToJsonb(mapper, buildArtifacts)),
                    DSL.value(serializeToJsonb(mapper, paramMap)),
                    DSL.value(idempotencyKey),
                )
                .returning(
                    USER_PROJECT_WORKFLOW.ID,
                    USER_PROJECT_WORKFLOW.PROJECT_ID,
                    USER_PROJECT_WORKFLOW.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.WORKFLOW_TYPE,
                    USER_PROJECT_WORKFLOW.LAST_STATUS,
                    USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.PLAN_INFO,
                    USER_PROJECT_WORKFLOW.COMMIT_INFO,
                    USER_PROJECT_WORKFLOW.REQUESTED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.VALUED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.BUILD_ARTIFACTS,
                    USER_PROJECT_WORKFLOW.PARAM_MAP,
                    USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY,
                )
        )
            .map { record ->
                record.into(UserProjectWorkflow::class.java)
            }
    }

    /**
     *     id                             bigserial    not null unique,
     *     project_id                     bigint       not null references user_project (id),
     *     creation_timestamp             bigint       not null,
     *     workflow_type                  varchar(511) not null, -- the kind of workflow
     *     last_status                    int          not null, -- the most recently recorded status
     *     last_status_recorded_timestamp bigint       not null,
     *     plan_info                      jsonb        not null,
     *     commit_info                    jsonb        not null,
     *     requested_user_inputs          jsonb        not null,
     *     valued_user_inputs             jsonb        not null,
     *     build_artifacts                jsonb        not null, -- outputs of the build
     *     param_map                      jsonb,                 -- input parameter map
     *     idempotency_key                varchar(511)
     */
    override fun update(
        workflowId: Long,
        updateRecord: UserProjectWorkflowRecord,
    ): Mono<UserProjectWorkflow> {
        return if (updateRecord.changed()) {
            Mono.from(
                dslContext
                    .update(
                        USER_PROJECT_WORKFLOW,
                    )
                    .set(
                        updateRecord
                    )
                    .where(
                        DSL.field(USER_PROJECT_WORKFLOW.ID.eq(DSL.value(workflowId)))
                    )
                    .returning(
                        USER_PROJECT_WORKFLOW.ID,
                        USER_PROJECT_WORKFLOW.PROJECT_ID,
                        USER_PROJECT_WORKFLOW.CREATION_TIMESTAMP,
                        USER_PROJECT_WORKFLOW.WORKFLOW_TYPE,
                        USER_PROJECT_WORKFLOW.LAST_STATUS,
                        USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP,
                        USER_PROJECT_WORKFLOW.PLAN_INFO,
                        USER_PROJECT_WORKFLOW.COMMIT_INFO,
                        USER_PROJECT_WORKFLOW.REQUESTED_USER_INPUTS,
                        USER_PROJECT_WORKFLOW.VALUED_USER_INPUTS,
                        USER_PROJECT_WORKFLOW.BUILD_ARTIFACTS,
                        USER_PROJECT_WORKFLOW.PARAM_MAP,
                        USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY,
                    )
            )
                .map { record ->
                    record.into(UserProjectWorkflow::class.java)
                }
        } else {
            get(workflowId)
        }
    }

    override fun get(
        workflowId: Long
    ): Mono<UserProjectWorkflow> {
        return Mono.from(
            dslContext
                .select(
                    USER_PROJECT_WORKFLOW.ID,
                    USER_PROJECT_WORKFLOW.PROJECT_ID,
                    USER_PROJECT_WORKFLOW.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.WORKFLOW_TYPE,
                    USER_PROJECT_WORKFLOW.LAST_STATUS,
                    USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.PLAN_INFO,
                    USER_PROJECT_WORKFLOW.COMMIT_INFO,
                    USER_PROJECT_WORKFLOW.REQUESTED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.VALUED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.BUILD_ARTIFACTS,
                    USER_PROJECT_WORKFLOW.PARAM_MAP,
                    USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY,
                )
                .from(USER_PROJECT_WORKFLOW)
                .where(
                    USER_PROJECT_WORKFLOW.ID.eq(DSL.value(workflowId))
                )
        )
            .map { record ->
                record.into(UserProjectWorkflow::class.java)
            }
    }

    override fun getForProject(
        projectId: Long,
    ): Flux<UserProjectWorkflow> {
        return Flux.from(
            dslContext
                .select(
                    USER_PROJECT_WORKFLOW.ID,
                    USER_PROJECT_WORKFLOW.PROJECT_ID,
                    USER_PROJECT_WORKFLOW.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.WORKFLOW_TYPE,
                    USER_PROJECT_WORKFLOW.LAST_STATUS,
                    USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.PLAN_INFO,
                    USER_PROJECT_WORKFLOW.COMMIT_INFO,
                    USER_PROJECT_WORKFLOW.REQUESTED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.VALUED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.BUILD_ARTIFACTS,
                    USER_PROJECT_WORKFLOW.PARAM_MAP,
                    USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY,
                )
                .from(USER_PROJECT_WORKFLOW)
                .where(
                    USER_PROJECT_WORKFLOW.PROJECT_ID.eq(DSL.value(projectId))
                )
        )
            .map { record ->
                record.into(UserProjectWorkflow::class.java)
            }
    }

    override fun getForProject(
        projectId: Long,
        workflowTypes: List<String>,
        earliestStatusRecorded: Long,
    ): Flux<UserProjectWorkflow> {
        return Flux.from(
            dslContext
                .select(
                    USER_PROJECT_WORKFLOW.ID,
                    USER_PROJECT_WORKFLOW.PROJECT_ID,
                    USER_PROJECT_WORKFLOW.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.WORKFLOW_TYPE,
                    USER_PROJECT_WORKFLOW.LAST_STATUS,
                    USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.PLAN_INFO,
                    USER_PROJECT_WORKFLOW.COMMIT_INFO,
                    USER_PROJECT_WORKFLOW.REQUESTED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.VALUED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.BUILD_ARTIFACTS,
                    USER_PROJECT_WORKFLOW.PARAM_MAP,
                    USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY,
                )
                .from(USER_PROJECT_WORKFLOW)
                .where(
                    USER_PROJECT_WORKFLOW.PROJECT_ID.eq(DSL.value(projectId)),
                    USER_PROJECT_WORKFLOW.WORKFLOW_TYPE.`in`(workflowTypes.map { DSL.value(it) }),
                    USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP.greaterOrEqual(DSL.value(earliestStatusRecorded)),
                )
        )
            .map { record ->
                record.into(UserProjectWorkflow::class.java)
            }
    }

    override fun getByIdempotencyKey(
        idempotencyKey: String,
    ): Mono<UserProjectWorkflow> {
        return Mono.from(
            dslContext
                .select(
                    USER_PROJECT_WORKFLOW.ID,
                    USER_PROJECT_WORKFLOW.PROJECT_ID,
                    USER_PROJECT_WORKFLOW.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.WORKFLOW_TYPE,
                    USER_PROJECT_WORKFLOW.LAST_STATUS,
                    USER_PROJECT_WORKFLOW.LAST_STATUS_RECORDED_TIMESTAMP,
                    USER_PROJECT_WORKFLOW.PLAN_INFO,
                    USER_PROJECT_WORKFLOW.COMMIT_INFO,
                    USER_PROJECT_WORKFLOW.REQUESTED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.VALUED_USER_INPUTS,
                    USER_PROJECT_WORKFLOW.BUILD_ARTIFACTS,
                    USER_PROJECT_WORKFLOW.PARAM_MAP,
                    USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY,
                )
                .from(USER_PROJECT_WORKFLOW)
                .where(
                    USER_PROJECT_WORKFLOW.IDEMPOTENCY_KEY.eq(DSL.value(idempotencyKey))
                )
        )
            .map { record ->
                record.into(UserProjectWorkflow::class.java)
            }
    }
}
