package com.arbr.engine.services.kafka.producer

import com.arbr.api.workflow.view_model.*
import com.arbr.api.workflow.view_model.update.ViewModelValueUpdate
import com.arbr.api.workflow.view_model.update.ViewModelValueUpdateOperation
import com.arbr.content_formats.mapper.Mappers
import com.arbr.kafka.topic.base.ApiKafkaProducerNode
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WorkflowPartialViewModelMessageProducer {

    fun submitWorkflowPartialViewModel(
        partialViewModel: WorkflowPartialViewModel,
    ): Mono<Void>

    fun submitWorkflowPartialViewModel(
        workflowId: Long,
        workflowStartTimeMs: Long?,
        progress: Double?,
        status: WorkflowViewModelStatus?,
        activeTasks: List<WorkflowViewModelActiveTask>?,
        fileData: List<WorkflowViewModelFileData>?,
        stats: List<WorkflowViewModelStatistic>?,
        pullRequest: WorkflowViewModelPullRequestData?,
        commits: List<WorkflowViewModelCommitData>?,
    ): Mono<Void> {
        val partialViewModel = WorkflowPartialViewModel(
            workflowId.toString(),
            updateSetOfNullable(workflowStartTimeMs),
            updateSetOfNullable(progress),
            updateSetOfNullable(status),
            updateSetOfNullable(activeTasks),
            updateSetOfNullable(fileData),
            updateSetOfNullable(stats),
            updateSetOfNullable(pullRequest),
            updateSetOfNullable(commits),
        )

        return submitWorkflowPartialViewModel(partialViewModel)
    }

    companion object {
        private fun <T : Any> updateSetOfNullable(
            nullableValue: T?
        ): ViewModelValueUpdate<T>? = nullableValue?.let { value ->
            ViewModelValueUpdate(
                ViewModelValueUpdateOperation.SET,
                value,
            )
        }
    }
}

class WorkflowPartialViewModelMessageProducerFactory(
    private val workflowPartialViewModelProducerNodes: List<ApiKafkaProducerNode<Long, WorkflowPartialViewModel>>,
) {

    fun makeWorkflowPartialViewModelMessageProducer(): WorkflowPartialViewModelMessageProducer {
        return WorkflowPartialViewModelMessageProducerImpl(workflowPartialViewModelProducerNodes)
    }

}

internal class WorkflowPartialViewModelMessageProducerImpl(
    private val workflowPartialViewModelProducerNodes: List<ApiKafkaProducerNode<Long, WorkflowPartialViewModel>>,
): WorkflowPartialViewModelMessageProducer {
    private val mapper = Mappers.mapper

    override fun submitWorkflowPartialViewModel(
        partialViewModel: WorkflowPartialViewModel,
    ): Mono<Void> {
        val workflowId = partialViewModel.workflowId.toLong()

        return Flux
            .fromIterable(workflowPartialViewModelProducerNodes)
            .concatMap { node ->
                node
                    .send(workflowId, partialViewModel)
            }
            .then()
            .doOnSuccess {
                logger.info("Sent workflow partial event: ${mapper.writeValueAsString(partialViewModel)}")
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowPartialViewModelMessageProducerImpl::class.java)
    }
}
