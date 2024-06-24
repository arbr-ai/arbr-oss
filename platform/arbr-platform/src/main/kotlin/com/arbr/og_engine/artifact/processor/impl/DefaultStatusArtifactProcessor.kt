package com.arbr.og_engine.artifact.processor.impl

import com.arbr.db.public.tables.references.USER_PROJECT_WORKFLOW
import com.arbr.engine.services.user.WorkflowStatusRepository
import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.engine.services.workflow.model.WorkflowStateUpdate
import com.arbr.og_engine.artifact.StatusArtifact
import com.arbr.og_engine.artifact.processor.base.StatusArtifactProcessor
import reactor.core.publisher.Mono
import java.time.Instant

class DefaultStatusArtifactProcessor(
    private val workflowStatusRepository: WorkflowStatusRepository,
) : StatusArtifactProcessor<WorkflowState> {
    override fun processArtifact(artifact: StatusArtifact, input: WorkflowState): Mono<WorkflowState> {
        val newState = input.copy(status = artifact.status)
        val update = WorkflowStateUpdate(input, newState, emptyList())

        val nowMs = Instant.now().toEpochMilli()
        val updateRecord = USER_PROJECT_WORKFLOW.newRecord().also { record ->
            record.lastStatus = newState.status.ordinal
            record.lastStatusRecordedTimestamp = nowMs
        }

        return workflowStatusRepository.update(
            update.newState.workflowId.toLong(),
            updateRecord,
        ).thenReturn(update.newState)
    }
}