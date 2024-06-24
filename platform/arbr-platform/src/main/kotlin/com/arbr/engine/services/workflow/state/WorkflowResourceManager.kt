package com.arbr.engine.services.workflow.state

import com.arbr.og_engine.core.WorkflowResourceModel
import com.arbr.util_common.collections.splitOn
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
@ComponentScan("com.arbr.og_engine")
class WorkflowResourceManager(
    private val workflowInitializationService: WorkflowInitializationService,
    private val workflowCancellationService: WorkflowCancellationService,

    // Workflow Handle ID -> Workflow Resource Model
    private val workflowResourceModels: ConcurrentHashMap<String, WorkflowResourceModel> = ConcurrentHashMap(),
) : WorkflowInitializingResourceManager by DefaultWorkflowInitializingResourceManager(
    workflowInitializationService,
    workflowResourceModels,
), WorkflowFinishingResourceManager by DefaultWorkflowFinishingResourceManager(workflowResourceModels) {

    /**
     * Map of all workflow handle IDs to resource models
     */
    @Synchronized
    fun getWorkflowResourceModels(): Mono<Map<String, WorkflowResourceModel>> {
        val models = workflowResourceModels.toMap()
        return Flux.fromIterable(models.values)
            .concatMap { model ->
                workflowCancellationService.workflowIsCancelled(model.workflowHandleId.toLong())
                    .map { isCancelled ->
                        model to isCancelled
                    }
            }
            .collectList()
            .map { modelsWithCancelledStatus ->
                val (cancelled, notCancelled) = modelsWithCancelledStatus.splitOn { it.second }

                synchronized(this) {
                    // TODO: Avoid side-effect
                    cancelled.forEach { workflowResourceModels.remove(it.first.workflowHandleId) }
                }

                notCancelled
                    .map { it.first }
                    .associateBy { it.workflowHandleId }
            }
    }

}
