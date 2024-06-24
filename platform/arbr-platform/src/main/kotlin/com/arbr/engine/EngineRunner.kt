package com.arbr.engine

import com.arbr.api.workflow.message.SubmitWorkflowTaskMessage
import com.arbr.engine.app.routine.WorkerRoutine
import com.arbr.engine.services.kafka.producer.WorkflowTaskMessageProducer
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.system.exitProcess

@Component
class EngineRunner(
    private val applicationContext: ConfigurableApplicationContext,
    private val workerRoutine: WorkerRoutine,
    @Value("\${arbr.engine.run-on-startup:false}")
    private val runOnStartup: Boolean,
    @Value("\${arbr.engine.exit-on-completion:true}")
    private val exitOnCompletion: Boolean,
    private val workflowTaskMessageProducer: WorkflowTaskMessageProducer,
) {
    private enum class ExecutionState {
        NOT_STARTED,
        STARTED,
        COMPLETED;
    }

    private var state = ExecutionState.NOT_STARTED
    private var processMono: Mono<Void>? = null

    @PostConstruct
    fun init() {
        if (runOnStartup) {
            startIfAble(null)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe()
        }
    }

    /**
     * Atomically set state to STARTED if currently NOT_STARTED, and return whether to actually start the process.
     */
    @Synchronized
    private fun setStartedIfAble(): Boolean {
        return if (state == ExecutionState.NOT_STARTED) {
            state = ExecutionState.STARTED
            true
        } else {
            false
        }
    }

    @Synchronized
    private fun setCompleted() {
        state = ExecutionState.COMPLETED
    }

    private fun startIfAble(
        inputWorkflowTaskMessage: SubmitWorkflowTaskMessage?
    ): Mono<Void> {
        return Mono.fromCallable {
            setStartedIfAble()
        }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { doStart ->
                if (doStart) {
                    val newProcessMono = runEngineInner()
                        .then()
                        .cache()

                    val submitMono = if (inputWorkflowTaskMessage == null) {
                        Mono.empty()
                    } else {
                        workflowTaskMessageProducer.submitWorkflowTask(
                            inputWorkflowTaskMessage.userId,
                            inputWorkflowTaskMessage.projectFullName,
                            inputWorkflowTaskMessage.workflowType,
                            inputWorkflowTaskMessage.requestCreationTimestampMs,
                            inputWorkflowTaskMessage.workflowId,
                            inputWorkflowTaskMessage.projectId,
                            inputWorkflowTaskMessage.workflowParams,
                        )
                    }

                    processMono = newProcessMono

                    submitMono.then(
                        newProcessMono
                    )
                } else {
                    processMono!!
                }
            }
    }

    private fun runWorker(): Mono<Boolean> {
        return workerRoutine.run()
            .doOnSubscribe {
                logger.info("Running worker")
            }
            .thenReturn(true)
    }

    private fun runEngineInner(): Mono<Boolean> {
        return runWorker()
            .doOnEach { doTerminateSignal ->
                setCompleted()
                if (doTerminateSignal.isOnError) {
                    logger.error("Encountered error during engine execution: ${doTerminateSignal.throwable}")
                    logger.error(doTerminateSignal.throwable?.stackTraceToString() ?: "No stacktrace")

                    if (exitOnCompletion) {
                        val exitCode: Int = SpringApplication.exit(applicationContext, ExitCodeGenerator {
                            1
                        })
                        exitProcess(exitCode)
                    }
                } else if (doTerminateSignal.isOnNext && doTerminateSignal.get()!! && exitOnCompletion) {
                    logger.info("Engine execution completed successfully - terminating")
                    val exitCode: Int = SpringApplication.exit(applicationContext, ExitCodeGenerator {
                        0
                    })
                    exitProcess(exitCode)
                } else {
                    logger.info("Engine execution completed successfully - not terminating")
                }
            }
    }

    /**
     * Begin running the engine for this app lifecycle if it has not already been started.
     */
    fun runEngine(
        inputWorkflowTaskMessage: SubmitWorkflowTaskMessage?
    ): Mono<Void> {
        return startIfAble(inputWorkflowTaskMessage)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EngineRunner::class.java)
    }

}
