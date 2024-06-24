package com.arbr.engine.services.workflow.state

import com.arbr.engine.services.kafka.producer.WorkflowPartialViewModelMessageProducer
import com.arbr.engine.services.user.WorkflowStatusRepository
import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.artifact.processor.base.WorkflowStateArtifactProcessorFactory
import com.arbr.util_common.reactor.nonBlocking
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Manage workflow states and their changes.
 */
interface WorkflowStateService {
    fun getWorkflowState(id: String): Mono<WorkflowState>

    fun getWorkflowStateForProject(
        projectId: Long,
        workflowTypes: List<String>,
        earliestStatusRecorded: Long,
    ): Flux<WorkflowState>

    fun putWorkflowArtifact(
        workflowId: String,
        workflowArtifact: Artifact,
    ): Mono<Void>

    fun putWorkflowArtifacts(
        workflowId: String,
        workflowArtifacts: List<Artifact>,
    ): Mono<Void>

    /**
     * Re-process orphan resources, i.e. ones which did not have a parent at creation time due to the lack of a
     * perfect order guarantee on events.
     * Should be called periodically through an external @Scheduled annotation
     */
    fun retryOrphanResources()
}

class WorkflowStateServiceFactory(
    private val workflowViewModelManagerFactory: WorkflowViewModelManagerFactory,
    private val workflowStatusRepository: WorkflowStatusRepository,
    private val workflowPartialViewModelMessageEngineProducer: WorkflowPartialViewModelMessageProducer,
    private val artifactProcessorWrapperFactory: WorkflowStateArtifactProcessorFactory,
) {
    fun makeWorkflowStateService(): WorkflowStateService {
        return WorkflowStateServiceImpl(
            workflowViewModelManagerFactory,
            workflowStatusRepository,
            workflowPartialViewModelMessageEngineProducer,
            artifactProcessorWrapperFactory,
        )
    }
}

/**
 * Manage workflow states and their changes.
 */
internal class WorkflowStateServiceImpl(
    private val workflowViewModelManagerFactory: WorkflowViewModelManagerFactory,
    private val workflowStatusRepository: WorkflowStatusRepository,
    private val workflowPartialViewModelMessageEngineProducer: WorkflowPartialViewModelMessageProducer,
    artifactProcessorWrapperFactory: WorkflowStateArtifactProcessorFactory,
) : WorkflowStateService {
    private val artifactProcessor = artifactProcessorWrapperFactory.makeArtifactProcessor { workflowId, artifact ->
        workflowArtifactQueue.add(workflowId to artifact)
    }

    private val workflowViewModelManagerMap = ConcurrentHashMap<String, WorkflowViewModelManager>()

    private val workflowArtifactQueue = ConcurrentLinkedQueue<Pair<String, Artifact>>()

    @Scheduled(fixedRate = 917L)
    @Synchronized
    override fun retryOrphanResources() {
        val frozenArtifacts = mutableListOf<Pair<String, Artifact>>()
        while (workflowArtifactQueue.isNotEmpty()) {
            val elt = workflowArtifactQueue.remove()
            frozenArtifacts.add(elt)
        }

        if (frozenArtifacts.isEmpty()) {
            return
        }

        Flux.fromIterable(frozenArtifacts.groupBy { it.first }.entries)
            .concatMap { (id, artifacts) ->
                processArtifacts(id, artifacts.map { it.second })
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    // Note: assumes id is numerical DB id
    private fun databaseWorkflowId(workflowId: String): Long = workflowId.toLong()

    override fun getWorkflowState(id: String): Mono<WorkflowState> =
        workflowStatusRepository.get(
            databaseWorkflowId(id)
        ).mapNotNull { WorkflowState.fromUserProjectWorkflow(it) }

    override fun getWorkflowStateForProject(
        projectId: Long,
        workflowTypes: List<String>,
        earliestStatusRecorded: Long,
    ): Flux<WorkflowState> = workflowStatusRepository.getForProject(
        projectId,
        workflowTypes,
        earliestStatusRecorded,
    ).mapNotNull { userProjectWorkflow ->
        WorkflowState.fromUserProjectWorkflow(userProjectWorkflow)
    }

    private fun processSingleArtifact(
        workflowId: String,
        workflowState: WorkflowState,
        artifact: Artifact,
    ): Mono<Optional<WorkflowState>> {
        return artifactProcessor
            .processArtifact(artifact, workflowState)
            .map { Optional.of(it) }
            .defaultIfEmpty(Optional.empty())
    }

    private fun processArtifacts(
        workflowId: String,
        workflowArtifacts: List<Artifact>,
    ): Mono<WorkflowState> {
        return workflowArtifacts.fold(getWorkflowState(workflowId)) { workflowStateMono, workflowArtifact ->
            workflowStateMono.flatMap { workflowState ->
                processSingleArtifact(workflowId, workflowState, workflowArtifact)
                    .flatMap { nextStateOpt ->
                        if (nextStateOpt.isPresent) {
                            Mono.just(nextStateOpt.get())
                        } else {
                            Mono.just(workflowState)
                        }
                    }
            }
        }
            .flatMap { workflowUpdatedState ->
                val workflowIdLong = workflowId.toLong()
                val viewModelManager = workflowViewModelManagerMap.computeIfAbsent(workflowId) {
                    workflowViewModelManagerFactory.makeWorkflowViewModelManager(workflowIdLong)
                }

                viewModelManager.ingestWorkflowArtifacts(
                    workflowIdLong,
                    workflowArtifacts,
                )
                    .concatMap { partialViewModel ->
                        workflowPartialViewModelMessageEngineProducer
                            .submitWorkflowPartialViewModel(partialViewModel)
                            .thenReturn(Unit)
                    }
                    .collectList()
                    .thenReturn(workflowUpdatedState)
            }
    }

    override fun putWorkflowArtifact(
        workflowId: String,
        workflowArtifact: Artifact,
    ): Mono<Void> =
        putWorkflowArtifacts(
            workflowId,
            listOf(workflowArtifact),
        )

    override fun putWorkflowArtifacts(
        workflowId: String,
        workflowArtifacts: List<Artifact>,
    ): Mono<Void> =
        nonBlocking {
            workflowArtifactQueue.addAll(
                workflowArtifacts.map { workflowId to it }
            )
        }.then()
}