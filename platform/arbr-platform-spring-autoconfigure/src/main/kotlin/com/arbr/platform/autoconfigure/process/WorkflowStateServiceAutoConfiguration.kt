package com.arbr.platform.autoconfigure.process

import com.arbr.api.workflow.view_model.WorkflowPartialViewModel
import com.arbr.engine.services.kafka.producer.WorkflowPartialViewModelMessageProducer
import com.arbr.engine.services.kafka.producer.WorkflowPartialViewModelMessageProducerFactory
import com.arbr.engine.services.user.*
import com.arbr.engine.services.workflow.state.WorkflowStateService
import com.arbr.engine.services.workflow.state.WorkflowStateServiceFactory
import com.arbr.engine.services.workflow.state.WorkflowViewModelManagerFactory
import com.arbr.kafka.topic.base.ApiKafkaProducerNode
import com.arbr.og_engine.artifact.processor.base.WorkflowStateArtifactProcessorFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@ConditionalOnMissingBean(WorkflowStateService::class)
class WorkflowStateServiceAutoConfiguration {

    private var workflowStateService: WorkflowStateService? = null

    @Scheduled(fixedRate = 917L)
    fun processResourcesPeriodically() {
        synchronized(this) {
            workflowStateService?.retryOrphanResources()
        }
    }

    @Bean
    fun workflowStateService(
        workflowViewModelManagerFactory: WorkflowViewModelManagerFactory,
        workflowStatusRepository: WorkflowStatusRepository,
        workflowPartialViewModelMessageEngineProducer: WorkflowPartialViewModelMessageProducer,
        artifactProcessorWrapperFactory: WorkflowStateArtifactProcessorFactory,
    ): WorkflowStateService {
        return synchronized(this) {
            val currentService = workflowStateService
            if (currentService != null) {
                return currentService
            }

            val factory = WorkflowStateServiceFactory(
                workflowViewModelManagerFactory,
                workflowStatusRepository,
                workflowPartialViewModelMessageEngineProducer,
                artifactProcessorWrapperFactory
            )

            factory.makeWorkflowStateService()
                .also { workflowStateService = it }
        }
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowPartialViewModelMessageProducer::class)
    fun workflowPartialViewModelMessageProducer(
        workflowPartialViewModelProducerNodes: List<ApiKafkaProducerNode<Long, WorkflowPartialViewModel>>,
    ): WorkflowPartialViewModelMessageProducer {
        val factory = WorkflowPartialViewModelMessageProducerFactory(
            workflowPartialViewModelProducerNodes
        )
        return factory.makeWorkflowPartialViewModelMessageProducer()
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowViewModelManagerFactory::class)
    fun workflowViewModelManagerFactory(): WorkflowViewModelManagerFactory {
        // No mappers were configured in the legacy implementation
        return WorkflowViewModelManagerFactory(
            emptyList(),
            emptyList(),
        )
    }

}


