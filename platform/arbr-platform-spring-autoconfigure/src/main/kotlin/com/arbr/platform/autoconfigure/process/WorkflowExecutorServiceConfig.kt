package com.arbr.platform.autoconfigure.process

//import com.arbr.engine.services.user.WorkflowStatusRepository
//import com.arbr.engine.services.workflow.state.WorkflowExecutorService
//import com.arbr.engine.services.workflow.state.WorkflowExecutorServiceImpl
//import com.arbr.engine.services.workflow.state.WorkflowInitializingResourceManager
//import com.arbr.engine.services.workflow.state.WorkflowStateService
//import com.arbr.engine.services.workflow.transducer.WorkflowTransducer
//import org.springframework.boot.autoconfigure.AutoConfiguration
//import org.springframework.boot.autoconfigure.AutoConfigureAfter
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//@Configuration
//@AutoConfigureAfter(WorkflowStateServiceAutoConfiguration::class)
//@ConditionalOnMissingBean(WorkflowExecutorService::class)
//class WorkflowExecutorServiceConfig {
//
//    @Bean
//    fun workerExecutorService(
//        workflowStateService: WorkflowStateService,
//        workflowInitializingResourceManager: WorkflowInitializingResourceManager,
//        workflowStatusRepository: WorkflowStatusRepository,
//        workflowTransducers: List<WorkflowTransducer<*, *>>,
//    ): WorkflowExecutorService {
//        return WorkflowExecutorServiceImpl(
//            workflowStateService,
//            workflowInitializingResourceManager,
//            workflowStatusRepository,
//            workflowTransducers,
//        )
//    }
//}