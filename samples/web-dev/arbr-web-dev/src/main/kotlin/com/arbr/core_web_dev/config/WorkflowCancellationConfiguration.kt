package com.arbr.core_web_dev.config
//
//import com.arbr.engine.services.workflow.state.WorkflowCancellationService
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.ComponentScan
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import reactor.core.publisher.Mono
//
//@Configuration
//@ComponentScan("com.arbr.object_model")
//class WorkflowCancellationConfiguration(
//) {
//
//    @Bean
//    @Primary
//    fun workflowCancellationService(): WorkflowCancellationService {
//        return object : WorkflowCancellationService {
//            override fun workflowIsCancelled(workflowId: Long): Mono<Boolean> {
//                return Mono.just(false)
//            }
//
//            override fun cancelWorkflow(workflowId: Long): Mono<Void> {
//                return Mono.empty()
//            }
//
//        }
//    }
//
//}
