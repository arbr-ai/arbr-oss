package com.arbr.engine.services.user

import com.arbr.db.public.tables.pojos.UserProjectWorkflowWorker
import com.arbr.db.public.tables.references.USER_PROJECT_WORKFLOW_WORKER
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

interface WorkflowWorkerRepository {
    /**
     * Try to claim the workflow by ID, returning a record if successful and empty if not.
     */
    fun attemptClaim(
        creationTimestamp: Long,
        workflowId: Long,
        workerUuid: String,
    ): Mono<UserProjectWorkflowWorker>
}

class WorkflowWorkerRepositoryFactory(
    private val dslContext: DSLContext
) {

    fun makeWorkflowWorkerRepository(): WorkflowWorkerRepository {
        return WorkflowWorkerRepositoryImpl(dslContext)
    }
}

internal class WorkflowWorkerRepositoryImpl(
    private val dslContext: DSLContext,
) : WorkflowWorkerRepository {

    /**
     * Try to claim the workflow by ID, returning a record if successful and empty if not.
     */
    override fun attemptClaim(
        creationTimestamp: Long,
        workflowId: Long,
        workerUuid: String,
    ): Mono<UserProjectWorkflowWorker> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_PROJECT_WORKFLOW_WORKER,
                    USER_PROJECT_WORKFLOW_WORKER.CREATION_TIMESTAMP,
                    USER_PROJECT_WORKFLOW_WORKER.WORKFLOW_ID,
                    USER_PROJECT_WORKFLOW_WORKER.WORKER_UUID,
                )
                .values(
                    DSL.value(creationTimestamp),
                    DSL.value(workflowId),
                    DSL.value(workerUuid),
                )
                .onConflict()
                .doNothing()
                .returning(DSL.asterisk())
        ).map { record ->
            record.into(UserProjectWorkflowWorker::class.java)
        }
    }
}
