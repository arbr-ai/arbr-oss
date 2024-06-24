package com.arbr.engine.services.kafka.consumer

import com.arbr.api.workflow.message.EndWorkflowTaskMessage
import com.arbr.api.workflow.message.SubmitWorkflowTaskMessage
import com.arbr.kafka.topic.base.ApiKafkaConsumerEvent
import com.arbr.kafka.topic.base.ApiKafkaConsumerNode
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
@ComponentScan("com.arbr.kafka.topic.base")
@ConditionalOnProperty(prefix = "arbr.kafka", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowTaskMessageConsumer(
    private val workflowEventConsumerNode: ApiKafkaConsumerNode<Unit, SubmitWorkflowTaskMessage>,
    private val workflowEndEventConsumerNode: ApiKafkaConsumerNode<Long, EndWorkflowTaskMessage>,
) {

    fun getAllSubmittedWorkflowTasks(eventFilter: (ApiKafkaConsumerEvent<Unit, SubmitWorkflowTaskMessage>) -> Mono<Boolean>): Flux<SubmitWorkflowTaskMessage> {
        return workflowEventConsumerNode
            .receive(Unit)
            .filterWhen(eventFilter)
            .doOnNext {
                logger.info("Received valid workflow task ${it.key}")
            }
            .map {
                it.messageObject.objectValue
            }

    }

    fun getNextSubmittedWorkflowTask(eventFilter: (ApiKafkaConsumerEvent<Unit, SubmitWorkflowTaskMessage>) -> Mono<Boolean>): Mono<SubmitWorkflowTaskMessage> {
        return workflowEventConsumerNode
            .receive(Unit)
            .filterWhen(eventFilter)
            .next()
            .doOnNext {
                logger.info("Received valid workflow task ${it.key}")
            }
            .map {
                it.messageObject.objectValue
            }
    }

    fun getNextSubmittedWorkflowTask(): Mono<SubmitWorkflowTaskMessage> {
        return getNextSubmittedWorkflowTask { Mono.just(true) }
    }

    fun getAllSubmittedWorkflowEndEvents(workflowId: Long): Flux<EndWorkflowTaskMessage> {
        return workflowEndEventConsumerNode
            .receive(workflowId)
            .doOnNext {
                logger.info("Received valid workflow task ${it.key}")
            }
            .map {
                it.messageObject.objectValue
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowTaskMessageConsumer::class.java)
    }

}
