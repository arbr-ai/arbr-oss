package com.arbr.core_web_dev.config

import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.*
import com.arbr.og_engine.artifact.processor.base.BaseDomainArtifactProcessor
import com.arbr.og_engine.artifact.processor.base.DomainArtifactProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

class DefaultWorkflowArtifactProcessor: BaseDomainArtifactProcessor<WorkflowArtifact, WorkflowArtifact, WorkflowState>(
    WorkflowArtifact::class.java
) {
    override fun processRootArtifact(
        artifact: WorkflowArtifact,
        input: WorkflowState
    ): Mono<WorkflowState> {
        val workflowId = input.workflowId
        return when (artifact) {
//            is WorkflowResourceCreationArtifact -> {
//                val parentObjectModelUuid = artifact.parentObjectModelUuid
//                val isOrphanMono = if (parentObjectModelUuid == null) {
//                    Mono.just(false)
//                } else {
//                    workflowResourceRepository.get(parentObjectModelUuid)
//                        .map { false }
//                        .defaultIfEmpty(true)
//                }
//
//                val resourceData = artifact.resourceData
//                    .entries
//                    .filter { it.value != null }
//                    .associate { it.key to it.value!! }
//
//                isOrphanMono.flatMap { isOrphan ->
//                    if (isOrphan) {
//                        artifactQueue.add(
//                            workflowId to artifact
//                        )
//                        Mono.empty()
//                    } else {
//                        workflowResourceRepository.upsertResource(
//                            databaseWorkflowId(workflowId),
//                            artifact.objectModelUuid,
//                            parentObjectModelUuid,
//                            artifact.resourceType.serializedName,
//                            resourceData,
//                        ).then()
//                    }
//                }.thenReturn(Optional.empty())
//            }

//            is WorkflowResourceUpdateArtifact -> {
//                val resourceData = artifact.resourceData
//                    .entries
//                    .filter { it.value != null }
//                    .associate { it.key to it.value!! }
//
//                workflowResourceRepository.updateResource(artifact.objectModelUuid, resourceData)
//                    .thenReturn(Optional.empty())
//            }

            is WorkflowPullRequestArtifact,
            is WorkflowCommitArtifact,
            is WorkflowPlanArtifact,
            is WorkflowRevertArtifact -> {
                // Could probably return empty? This maintains legacy behavior
                Mono.just(input)
            }
        }
        
    }
}

@Configuration
class WorkflowStateDomainArtifactProcessorConfiguration {

    @Bean
    fun domainArtifactProcessor(): DomainArtifactProcessor<WorkflowArtifact, WorkflowArtifact, WorkflowState> {
        return DefaultWorkflowArtifactProcessor()
    }

}
