package com.arbr.engine.services.workflow.state

import com.arbr.api.workflow.core.WorkflowStatus
import com.arbr.api.workflow.core.WorkflowType
import com.arbr.api.workflow.input.WorkflowInputModel
import com.arbr.engine.services.user.WorkflowStatusRepository
import com.arbr.engine.services.workflow.transducer.WorkflowTransducer
import com.arbr.engine.util.FluxPool
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.artifact.StatusArtifact
import com.arbr.og_engine.core.WorkflowResourceModel
import com.arbr.og_engine.file_system.VolumeState
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

class WorkflowExecutorServiceImpl(
    private val workflowStateService: WorkflowStateService,
    private val workflowResourceManager: WorkflowInitializingResourceManager,
    private val workflowStatusRepository: WorkflowStatusRepository,
    private val workflowTransducers: List<WorkflowTransducer<*, *>>,
) : WorkflowExecutorService {

    private fun getMatchingTransducer(workflowType: WorkflowType): WorkflowTransducer<*, *>? {
        return workflowTransducers.firstOrNull {
            it.userFacingDisplayInfo.id == workflowType.serializedName
        }
    }

    private fun <WI : WorkflowInputModel, PI : Any> initiateWorkflow(
        userId: Long,
        workflowId: Long,
        projectFullName: String,
        workflowTransducer: WorkflowTransducer<WI, PI>,
        workflowFormInput: WI,
    ): Flux<Artifact> {
        val workflowHandleId = workflowId.toString()
        return Flux.deferContextual { contextView ->
            Flux.create { fluxSink ->
                workflowTransducer.setUp(
                    workflowHandleId,
                    workflowFormInput,
                    fluxSink,
                )
                    .onErrorMap {
                        Exception("Failure during workflow setup", it)
                    }
                    .flatMap { (volumeState, parsedInput) ->
                        buildInitialProjectIndex(
                            userId,
                            workflowHandleId,
                            volumeState,
                            projectFullName,
                            fluxSink,
                        )
                            .onErrorMap {
                                Exception("Failure during workflow initialization", it)
                            }
                            .flatMap { workflowResourceModel ->
                                workflowTransducer
                                    .perform(
                                        workflowHandleId,
                                        workflowResourceModel,
                                        volumeState,
                                        parsedInput,
                                        fluxSink,
                                    )
                                    .onErrorMap {
                                        Exception("Failure during workflow perform", it)
                                    }
                            }
                    }
                    .doOnError { ex ->
                        fluxSink.next(
                            StatusArtifact(
                                WorkflowStatus.FAILED,
                                value = ex.message,
                                throwable = ex,
                            )
                        )
                    }
                    .doOnSubscribe {
                        fluxSink.next(
                            StatusArtifact(
                                WorkflowStatus.STARTED,
                                value = null,
                                throwable = null,
                            )
                        )
                    }
                    .cache()
                    .contextWrite(contextView)
                    .contextWrite { context ->
                        context.put(WorkflowExecutorService.WORKFLOW_ID_CONTEXT_KEY, workflowHandleId)
                    }
                    .subscribeOn(workflowArtifactScheduler)
                    .publishOn(workflowArtifactScheduler)
                    .subscribe()
            }
        }
    }

    private fun ingestArtifacts(
        workflowId: Long,
        artifactFlux: Flux<Artifact>,
    ): Mono<Void> {

        // Fire and forget
        // TODO: Distinct on resource
        val stateUpdateFlux = FluxPool.create(
            mutableListOf(),
            artifactFlux,
        )
            .concatMap { workflowArtifacts ->
                Mono.defer {
                    workflowStateService.putWorkflowArtifacts(workflowId.toString(), workflowArtifacts)
                }
            }
            .then()

        return Mono.deferContextual { contextView ->
            stateUpdateFlux
                .cache()
                .contextWrite(contextView)
                .subscribeOn(workflowArtifactScheduler)
                .publishOn(workflowArtifactScheduler)
                .subscribe()
                .toMono()
        }.then()
    }

    private fun buildInitialProjectIndex(
        userId: Long,
        workflowHandleId: String,
        volumeState: VolumeState,
        projectFullName: String,
        artifactSink: FluxSink<Artifact>,
    ): Mono<WorkflowResourceModel> {
        return workflowResourceManager.createWorkflowResourceModel(
            userId,
            workflowHandleId,
            projectFullName,
            volumeState,
            artifactSink,
            preloadFromWorkflowHandleId = null,
        )
    }

    /**
     * Actually perform a registered workflow - intended for worker contexts.
     */
    private fun <WI : WorkflowInputModel, PI : Any> performRegisteredWorkflow(
        userId: Long,
        workflowId: Long,
        projectName: String,
        workflowTransducer: WorkflowTransducer<WI, PI>,
        paramMap: Map<String, String>,
    ): Mono<Void> {
        return workflowTransducer.parsing(paramMap) { workflowFormInput ->
            Mono.defer {
                val artifactFlux = initiateWorkflow(
                    userId,
                    workflowId,
                    projectName,
                    workflowTransducer,
                    workflowFormInput,
                )

                ingestArtifacts(workflowId, artifactFlux)
            }
        }.then()
    }

    override fun handleSubmittedWorkflowTask(
        userId: Long,
        projectFullName: String,
        workflowType: WorkflowType,
        workflowId: Long,
        projectId: Long,
        requestCreationTimestampMs: Long,
        workflowParams: Map<String, Any>,
    ): Mono<Void> {
        val workflowTransducer = getMatchingTransducer(workflowType)

        return if (workflowTransducer == null) {
            Mono.error(Exception())
        } else {
            val stringValueParamMap = workflowParams.mapValues { it.toString() }

            performRegisteredWorkflow(
                userId,
                workflowId,
                projectFullName,
                workflowTransducer,
                stringValueParamMap,
            )
        }
    }

    override fun getWorkflowStatus(workflowId: Long): Mono<WorkflowStatus> {
        return workflowStatusRepository.get(workflowId).mapNotNull { workflow ->
            WorkflowStatus.values().firstOrNull {
                it.ordinal == workflow.lastStatus
            }
        }
    }

    companion object {
        protected val workflowArtifactScheduler: Scheduler = Schedulers.newBoundedElastic(
            4,
            100_000,
            "artifact",
        )
    }
}