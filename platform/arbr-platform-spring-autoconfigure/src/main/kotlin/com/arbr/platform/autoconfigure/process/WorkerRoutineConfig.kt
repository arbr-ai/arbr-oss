package com.arbr.platform.autoconfigure.process

import com.arbr.engine.app.routine.WorkerRoutine
import com.arbr.engine.app.routine.WorkerRoutineImpl
import com.arbr.engine.services.kafka.consumer.WorkflowTaskMessageConsumer
import com.arbr.engine.services.user.WorkflowStatusRepository
import com.arbr.engine.services.user.WorkflowWorkerRepository
import com.arbr.engine.services.workflow.setup.WorkflowCredentialsProvider
import com.arbr.engine.services.workflow.state.WorkflowExecutorService
import com.arbr.engine.services.workflow.state.WorkflowExecutorServiceImpl
import com.arbr.engine.services.workflow.state.WorkflowFinishingResourceManager
import com.arbr.engine.services.workflow.state.WorkflowInitializingResourceManager
import com.arbr.engine.services.workflow.state.WorkflowStateService
import com.arbr.engine.services.workflow.transducer.WorkflowTransducer
import com.arbr.platform.object_graph.artifact.Artifact
import com.arbr.platform.object_graph.core.WorkflowResourceModel
import com.arbr.platform.object_graph.file_system.VolumeState
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

@Configuration
@AutoConfigureAfter(WorkflowStateServiceAutoConfiguration::class)
//@ConditionalOnMissingBean(WorkerRoutine::class)
class WorkerRoutineConfig {

    @Bean
    @ConditionalOnMissingBean(value = [WorkflowCredentialsProvider::class])
    fun workflowCredentialsDefaultProvider(): WorkflowCredentialsProvider {
        logger.info("Using default WorkflowCredentialsProvider configuration")

        return object : WorkflowCredentialsProvider {
            override fun getUserWorkflowCredentials(userId: Long, workflowId: Long): Mono<Map<String, Any>> {
                return Mono.just(emptyMap())
            }
        }
    }

    @Bean
    fun workflowInitializingResourceManager(): WorkflowInitializingResourceManager {
        return object : WorkflowInitializingResourceManager {
            override fun createWorkflowResourceModel(
                userId: Long,
                workflowHandleId: String,
                projectFullName: String,
                volumeState: VolumeState,
                artifactSink: FluxSink<Artifact>,
                preloadFromWorkflowHandleId: Long?
            ): Mono<WorkflowResourceModel> {
                TODO("Not yet implemented")
            }
        }
    }

    @Bean
    fun workflowFinishingResourceManager(): WorkflowFinishingResourceManager {
        return object : WorkflowFinishingResourceManager {
            override fun cancelWorkflow(workflowHandleId: String): Mono<Void> {
                TODO("Not yet implemented")
            }

            override fun finishWorkflow(workflowHandleId: String): Mono<Void> {
                TODO("Not yet implemented")
            }
        }
    }

    @Bean
    fun workerExecutorService(
        workflowStateService: WorkflowStateService,
        workflowInitializingResourceManager: WorkflowInitializingResourceManager,
        workflowStatusRepository: WorkflowStatusRepository,
        workflowTransducers: List<WorkflowTransducer<*, *>>,
    ): WorkflowExecutorService {
        return WorkflowExecutorServiceImpl(
            workflowStateService,
            workflowInitializingResourceManager,
            workflowStatusRepository,
            workflowTransducers,
        )
    }

    @Bean
    fun workerRoutine(
        workflowExecutorService: WorkflowExecutorService,
        workflowStateService: WorkflowStateService,
        workflowFinishingResourceManager: WorkflowFinishingResourceManager,
        workflowWorkerRepository: WorkflowWorkerRepository,
        workflowCredentialsProvider: WorkflowCredentialsProvider,
        @Autowired(required = false)
        taskMessageConsumer: WorkflowTaskMessageConsumer?,
    ): WorkerRoutine {
        return WorkerRoutineImpl(
            workflowExecutorService,
            workflowStateService,
            workflowFinishingResourceManager,
            workflowWorkerRepository,
            workflowCredentialsProvider,
            taskMessageConsumer,
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkerRoutineConfig::class.java)
    }
}
