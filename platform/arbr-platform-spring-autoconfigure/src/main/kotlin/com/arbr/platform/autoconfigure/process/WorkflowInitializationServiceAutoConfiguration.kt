package com.arbr.platform.autoconfigure.process

import com.arbr.engine.services.workflow.state.WorkflowInitializationService
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.WorkflowResourceModel
import com.arbr.og_engine.file_system.VolumeState
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

@Configuration
@ConditionalOnMissingBean(WorkflowInitializationService::class)
class WorkflowInitializationServiceAutoConfiguration {

    @Bean
    fun workflowInitializationDefaultService(): WorkflowInitializationService {
        logger.warn("Using default WorkflowInitializationService configuration")

        return object : WorkflowInitializationService {
            override fun beginUpdates(
                workflowResourceModel: WorkflowResourceModel,
                volumeState: VolumeState,
                artifactSink: FluxSink<Artifact>
            ): Mono<Void> {
                // TODO
                return Mono.empty()
            }

            override fun createWorkflowResourceModel(
                userId: Long,
                workflowHandleId: String,
                projectFullName: String,
                volumeState: VolumeState,
                artifactSink: FluxSink<Artifact>,
                preloadFromWorkflowHandleId: Long?
            ): WorkflowResourceModel {
                // Right now this requires constructing a domain object to be the root, plus at least one other object
                // to kick off inferences - could this be made simpler if we just allow an empty graph?
                throw NotImplementedError("WorkflowInitializationService not configured")
            }


        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowInitializationServiceAutoConfiguration::class.java)
    }

}