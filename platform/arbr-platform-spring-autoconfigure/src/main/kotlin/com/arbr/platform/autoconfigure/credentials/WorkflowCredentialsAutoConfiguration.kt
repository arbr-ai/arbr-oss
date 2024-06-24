package com.arbr.platform.autoconfigure.credentials

import com.arbr.engine.services.workflow.setup.WorkflowCredentialsProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnMissingBean(value = [WorkflowCredentialsProvider::class])
class WorkflowCredentialsAutoConfiguration {

//    @Bean
//    fun workflowCredentialsDefaultProvider(): WorkflowCredentialsProvider {
//        logger.info("Using default WorkflowCredentialsProvider configuration")
//
//        return object : WorkflowCredentialsProvider {
//            override fun getUserWorkflowCredentials(userId: Long, workflowId: Long): Mono<Map<String, Any>> {
//                return Mono.just(emptyMap())
//            }
//        }
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(WorkflowCredentialsAutoConfiguration::class.java)
//    }

}
