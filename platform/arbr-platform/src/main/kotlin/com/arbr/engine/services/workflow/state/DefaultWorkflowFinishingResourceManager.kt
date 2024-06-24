package com.arbr.engine.services.workflow.state

import com.arbr.og_engine.core.WorkflowResourceModel
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

class DefaultWorkflowFinishingResourceManager(
    private val workflowResourceModels: ConcurrentHashMap<String, WorkflowResourceModel>,
): WorkflowFinishingResourceManager {
    private fun endWorkflow(
        workflowHandleId: String,
        finishedWithSuccess: Boolean,
    ): Mono<Void> {
        // TODO
        return Mono.empty()

//        return Mono.defer {
//            // Note this only cancels in memory, i.e. if the workflow is running on this task
//            val task = workflowResourceModels[workflowHandleId]?.projectTask
//
//            if (task == null) {
//                logger.warn("Did not find workflow $workflowHandleId to cancel")
//                Mono.empty()
//            } else {
//                task.taskEvals.items.proposeAsync {
//                    val status = if (finishedWithSuccess) {
//                        logger.info("Marking workflow $workflowHandleId as finished early")
//                        taskCompletionStatusFinishedEarly(task)
//                    } else {
//                        logger.info("Marking workflow $workflowHandleId as cancelled")
//                        taskCompletionStatusCancelled(task)
//                    }
//                    val ref = ObjectRef.OfResource(status)
//                    Mono.just(
//                        (it ?: ImmutableLinkedMap()).adding(
//                            status.uuid, ref
//                        )
//                    )
//                }
//            }.then(
//                Mono.fromCallable {
//                    // Finished-early tasks complete organically
//                    if (!finishedWithSuccess) {
//                        workflowResourceModels.remove(workflowHandleId)
//                    }
//                }.subscribeOn(Schedulers.boundedElastic())
//            )
//        }
//            .then()
    }

    override fun cancelWorkflow(
        workflowHandleId: String,
    ): Mono<Void> {
        return endWorkflow(workflowHandleId, finishedWithSuccess = false)
    }

    override fun finishWorkflow(
        workflowHandleId: String,
    ): Mono<Void> {
        return endWorkflow(workflowHandleId, finishedWithSuccess = true)
    }
}