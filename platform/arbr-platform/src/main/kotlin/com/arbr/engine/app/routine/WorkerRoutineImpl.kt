package com.arbr.engine.app.routine

import com.arbr.api.workflow.core.WorkflowStatus
import com.arbr.api.workflow.message.SubmitWorkflowTaskMessage
import com.arbr.engine.services.kafka.consumer.WorkflowTaskMessageConsumer
import com.arbr.engine.services.user.WorkflowWorkerRepository
import com.arbr.engine.services.workflow.setup.WorkflowCredentialsProvider
import com.arbr.engine.services.workflow.state.WorkflowExecutorService
import com.arbr.engine.services.workflow.state.WorkflowFinishingResourceManager
import com.arbr.engine.services.workflow.state.WorkflowStateService
import com.arbr.og_engine.artifact.StatusArtifact
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeoutException

class WorkerRoutineImpl(
    private val workflowExecutorService: WorkflowExecutorService,
    private val workflowStateService: WorkflowStateService,
    private val workflowFinishingResourceManager: WorkflowFinishingResourceManager,
    private val workflowWorkerRepository: WorkflowWorkerRepository,
    private val workflowCredentialsProvider: WorkflowCredentialsProvider,
    private val taskMessageConsumer: WorkflowTaskMessageConsumer?,
) : WorkerRoutine {
    private val workerUuid = UUID.randomUUID().toString()

    /**
     * Fake exception to exit the waiting process
     */
    private class WorkflowCompleteException(
        val workflowId: Long,
        val workflowStatus: WorkflowStatus,
    ) : Exception("Workflow $workflowId completed with status ${workflowStatus.serializedName}")

    private fun shutdownAfterDelay(): Mono<Void> {
        return Mono.defer {
            Mono.delay(minShutdownDelay)
                .doOnSubscribe {
                    logger.info("Waiting ${minShutdownDelay.toSeconds()}s before shutdown...")
                }
        }.then()
    }

    private fun listenForCancellation(workflowId: Long): Flux<Unit> {
        if (taskMessageConsumer == null) {
            return Flux.empty()
        }

        return taskMessageConsumer.getAllSubmittedWorkflowEndEvents(workflowId)
            .flatMap { endWorkflowTaskMessage ->
                val workflowHandleId = endWorkflowTaskMessage.workflowId.toString()

                when (endWorkflowTaskMessage.workflowStatus) {
                    WorkflowStatus.NOT_STARTED,
                    WorkflowStatus.STARTED,
                    WorkflowStatus.FAILED,
                    WorkflowStatus.CANCELLED -> {
                        workflowStateService.putWorkflowArtifact(
                            workflowHandleId,
                            StatusArtifact(
                                WorkflowStatus.CANCELLED,
                                "Workflow cancelled",
                                null,
                            )
                        ).then(
                            workflowFinishingResourceManager.cancelWorkflow(workflowHandleId)
                        ).thenReturn(Unit)
                    }

                    WorkflowStatus.SUCCEEDED -> {
                        // Mark the workflow as finished gracefully via task completion status so that a PR can be
                        // opened. If all is well, the task should succeed naturally after opening the PR, via
                        // the WorkflowTaskStatusPublisher.
                        val finishMono = workflowFinishingResourceManager.finishWorkflow(workflowHandleId)

                        // It wouldn't be a horrible idea to fall back to cancelling after a period of time, but it can
                        // take a little bit to get the pull request open, so we're waiting for now. The user can still
                        // hit cancel.
                        finishMono
                            .flatMapMany {
                                Flux.empty()
                            }
                    }
                }
            }
    }

    private fun workflowApiStatus(status: WorkflowStatus): WorkflowStatus {
        return when (status) {
            WorkflowStatus.NOT_STARTED -> WorkflowStatus.NOT_STARTED
            WorkflowStatus.STARTED -> WorkflowStatus.STARTED
            WorkflowStatus.SUCCEEDED -> WorkflowStatus.SUCCEEDED
            WorkflowStatus.FAILED -> WorkflowStatus.FAILED
            WorkflowStatus.CANCELLED -> WorkflowStatus.CANCELLED
        }
    }

    private fun awaitCompletion(workflowId: Long): Flux<Unit> {
        return Flux.interval(workflowCompletionPollInterval)
            .flatMap {
                workflowExecutorService.getWorkflowStatus(workflowId)
                    .flatMap { status ->
                        when (val apiStatus = workflowApiStatus(status)) {
                            WorkflowStatus.NOT_STARTED,
                            WorkflowStatus.STARTED -> Mono.empty()

                            WorkflowStatus.SUCCEEDED,
                            WorkflowStatus.FAILED,
                            WorkflowStatus.CANCELLED -> Mono.error<Unit>(
                                WorkflowCompleteException(
                                    workflowId,
                                    apiStatus
                                )
                            )
                        }
                    }
            }
    }

    /**
     * Attempt to claim a workflow atomically. Return true if successful, false if already claimed, error if hit an
     * error.
     */
    private fun attemptClaim(
        workflowId: Long,
    ): Mono<Boolean> {
        val nowMs = Instant.now().toEpochMilli()

        return workflowWorkerRepository.attemptClaim(
            nowMs,
            workflowId,
            workerUuid,
        )
            .filter {
                // Double check we are the worker who claimed it (should already be enforced by the DB)
                it.workerUuid == workerUuid
            }
            .materialize()
            .flatMap { signal ->
                if (signal.isOnNext) {
                    Mono.just(true)
                } else if (signal.isOnError) {
                    Mono.error(signal.throwable!!)
                } else {
                    Mono.empty()
                }
            }
    }

    private fun handleMessage(message: SubmitWorkflowTaskMessage): Mono<Void> {
        return attemptClaim(message.workflowId)
            .flatMap { claimed ->
                if (claimed) {
                    workflowCredentialsProvider.getUserWorkflowCredentials(message.userId, message.workflowId)
                        .flatMap { credentialsMap ->
                            workflowExecutorService.handleSubmittedWorkflowTask(
                                message.userId,
                                message.projectFullName,
                                message.workflowType,
                                message.workflowId,
                                message.projectId,
                                message.requestCreationTimestampMs,
                                message.workflowParams,
                            )
                                .contextWrite { context ->
                                    context.putAllMap(credentialsMap)
                                }
                        }
                        .then(
                            Mono.defer {
                                Flux.zip(
                                    listenForCancellation(message.workflowId),
                                    awaitCompletion(message.workflowId),
                                )
                                    .onErrorResume(WorkflowCompleteException::class.java) {
                                        logger.info(it.message)
                                        Mono.empty()
                                    }
                                    .then(shutdownAfterDelay())
                            }
                        )
                } else {
                    Mono.empty()
                }
            }
    }

    override fun run(): Mono<Void> {
        if (taskMessageConsumer == null) {
            logger.warn("Kafka consumer is absent - not running worker")
            return Mono.error(Exception("No Kafka consumer for worker"))
        }

        return taskMessageConsumer
            .getNextSubmittedWorkflowTask {
                val delayMs = Instant.now().toEpochMilli() - it.messageObject.objectValue.requestCreationTimestampMs
                Mono.just(Duration.ofMillis(delayMs) < maxWorkflowDelay)
            }
            .doOnSubscribe {
                logger.info("Waiting for workflow task message...")
            }
            .timeout(workflowSubmitAcquireTimeLimit)
            .doOnNext {
                val delayMs = Instant.now().toEpochMilli() - it.requestCreationTimestampMs
                logger.info("Received workflow task message! ${it.userId} ${it.projectFullName} ${it.workflowType.serializedName} [delay=${delayMs}ms]")
            }
            .flatMap {
                handleMessage(it)
            }
            .onErrorResume(TimeoutException::class.java) {
                logger.info("Timed out waiting ${workflowSubmitAcquireTimeLimit.toSeconds()}s for a workflow job")

                Mono.empty()
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkerRoutineImpl::class.java)

        /**
         * Ignore unacknowledged workflow requests with a delay of at least 15 minutes.
         */
        private val maxWorkflowDelay = Duration.ofMinutes(15L)

        private val minShutdownDelay = Duration.ofSeconds(10L)

        private val workflowCompletionPollInterval = Duration.ofMillis(2133L)

        /**
         * Time limit for waiting for a workflow request before terminating the task.
         */
        private val workflowSubmitAcquireTimeLimit = Duration.ofMinutes(3600L)
    }
}