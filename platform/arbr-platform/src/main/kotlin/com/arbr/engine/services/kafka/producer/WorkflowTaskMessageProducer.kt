package com.arbr.engine.services.kafka.producer

import com.arbr.api.workflow.core.WorkflowStatus
import com.arbr.api.workflow.core.WorkflowType
import com.arbr.api.workflow.message.EndWorkflowTaskMessage
import com.arbr.api.workflow.message.SubmitWorkflowTaskMessage
import com.arbr.kafka.topic.base.ApiKafkaProducerNode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WorkflowTaskMessageProducer(
    private val workflowEventProducerNode: ApiKafkaProducerNode<Unit, SubmitWorkflowTaskMessage>?,
    private val workflowEndEventProducerNode: ApiKafkaProducerNode<Long, EndWorkflowTaskMessage>?,
) {

    fun submitWorkflowTask(
        userId: Long,
        projectFullName: String,
        workflowType: WorkflowType,
        creationTimestampMs: Long,
        workflowId: Long,
        projectId: Long,
        paramMap: Map<String, Any>,
    ): Mono<Void> {
        val node = workflowEventProducerNode
            ?: return Mono.empty()

        val message = SubmitWorkflowTaskMessage(
            userId,
            projectFullName,
            workflowType,
            creationTimestampMs,
            workflowId,
            projectId,
            paramMap,
        )

        return node
            .send(Unit, message)
            .doOnSuccess {
                val logId = listOf(
                    userId.toString(),
                    projectFullName,
                    workflowType.serializedName,
                    creationTimestampMs.toString(),
                ).joinToString(":") {
                    it
                        .replace("/", "_")
                        .replace(":", "_")
                }

                logger.info("Sent workflow submit message $logId")
            }
    }

    fun endWorkflowTask(
        userId: Long,
        workflowId: Long,
        workflowStatus: WorkflowStatus,
    ): Mono<Void> {
        val node = workflowEndEventProducerNode
            ?: return Mono.empty()

        val message = EndWorkflowTaskMessage(
            userId,
            workflowId,
            workflowStatus,
        )

        return node
            .send(workflowId, message)
            .doOnSuccess {
                val logId = listOf(
                    userId.toString(),
                    workflowId.toString(),
                    workflowStatus.serializedName,
                ).joinToString(":") {
                    it
                        .replace("/", "_")
                        .replace(":", "_")
                }

                logger.info("Sent workflow end message $logId")
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowTaskMessageProducer::class.java)
    }

}
