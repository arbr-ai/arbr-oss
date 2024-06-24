package com.arbr.platform.autoconfigure.process

import com.arbr.engine.services.workflow.state.WorkflowCancellationService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono


@Configuration
@ConditionalOnMissingBean(value = [WorkflowCancellationService::class])
class WorkflowCancellationServiceAutoConfiguration {

    @Bean
    fun workflowCancellationDefaultService(): WorkflowCancellationService {
        logger.info("Using default WorkflowCancellationService configuration")

        return object : WorkflowCancellationService {
            override fun workflowIsCancelled(workflowId: Long): Mono<Boolean> {
                return Mono.just(false)
            }

            override fun cancelWorkflow(workflowId: Long): Mono<Void> {
                return Mono.empty()
            }

        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowCancellationServiceAutoConfiguration::class.java)
    }

}

