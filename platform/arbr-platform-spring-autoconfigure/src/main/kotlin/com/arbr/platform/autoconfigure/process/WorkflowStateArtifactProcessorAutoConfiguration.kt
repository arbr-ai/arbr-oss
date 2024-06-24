package com.arbr.platform.autoconfigure.process

import com.arbr.engine.services.user.WorkflowResourceRepository
import com.arbr.engine.services.user.WorkflowStatusRepository
import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.processor.base.CombinedArtifactProcessor
import com.arbr.og_engine.artifact.processor.base.DomainArtifactProcessor
import com.arbr.og_engine.artifact.processor.base.WorkflowStateArtifactProcessorFactory
import com.arbr.og_engine.artifact.processor.impl.*
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnMissingBean(WorkflowStateArtifactProcessorFactory::class)
@AutoConfigureAfter(DbResourceStoreAutoConfiguration::class)
class WorkflowStateArtifactProcessorAutoConfiguration {

    @Bean
    fun artifactProcessorFactory(
        workflowResourceRepository: WorkflowResourceRepository,
        workflowStatusRepository: WorkflowStatusRepository,

        domainArtifactProcessors: List<DomainArtifactProcessor<*, *, WorkflowState>>,
    ): WorkflowStateArtifactProcessorFactory {
        return WorkflowStateArtifactProcessorFactory { orphanArtifactQueueDelegate ->
            val processorStatusArtifactProcessor = DefaultProcessorStatusArtifactProcessor()
            val statusArtifactProcessor = DefaultStatusArtifactProcessor(workflowStatusRepository)
            val applicationCompletionArtifactProcessor = DefaultApplicationCompletionArtifactProcessor()
            val workflowResourceCreationArtifactProcessor = DefaultWorkflowResourceCreationArtifactProcessor(
                workflowResourceRepository,
                orphanArtifactQueueDelegate,
            )
            val workflowResourceUpdateArtifactProcessor = DefaultWorkflowResourceUpdateArtifactProcessor(
                workflowResourceRepository,
            )

            CombinedArtifactProcessor(
                processorStatusArtifactProcessor,
                statusArtifactProcessor,
                applicationCompletionArtifactProcessor,
                workflowResourceCreationArtifactProcessor,
                workflowResourceUpdateArtifactProcessor,
                domainArtifactProcessors,
            )
        }
    }

}
