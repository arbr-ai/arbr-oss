package com.arbr.platform.object_graph.core

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedValue
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.og_engine.artifact.Artifact
import com.arbr.platform.object_graph.concurrency.AtomicStack
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.platform.object_graph.arbiter.WorkflowResourceArbiter
import com.arbr.util_common.reactor.nonBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import reactor.util.context.Context
import reactor.util.context.ContextView
import java.util.concurrent.ConcurrentHashMap
import com.arbr.platform.object_graph.generics.WorkflowCancellationService
import com.arbr.platform.object_graph.generics.WorkflowResourceLifecycleService

class OperationRetriesExhaustedException(val name: String, val resourceUuid: String, override val cause: Throwable?) :
    Exception("Retries exhausted on ${name}:${resourceUuid}", cause)

abstract class WorkflowSingleResourceProcessor<T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>(
    private val cancellationService: WorkflowCancellationService,
    private val lifecycleService: WorkflowResourceLifecycleService,
    val unaryFunctions: List<WorkflowResourceUnaryFunction<T, P, *, *, *, *, ForeignKey>>,
    private val arbiter: WorkflowResourceArbiter<T>,
    private val propertyValueLockManager: PropertyValueLockManager,
    private val maxEdgeRetries: Int,
    private val resourceLogger: Logger,
) {
    abstract val resourceDisplayName: String

    private val resourceStore = ConcurrentHashMap<String, T>()

    private val resourceOperations = ConcurrentHashMap<String, List<ResourceOperationStatus<T, P, ForeignKey>>>()

    private val arbitrateProcessLockMap = ConcurrentHashMap<String, Mono<Void>>()

    fun handleResourcePropertyValueUpdate(newValue: ProposedValue<*>) {
        val key =
            "${newValue.identifier.resourceKey.name}[${newValue.identifier.resourceUuid}].${newValue.identifier.propertyKey.name}"

        resourceOperations.forEachEntry(8) { entry ->
            entry.value.forEach { status ->
                // If the status is awaiting a dependency that had an update, mark it as expected satisfied
                val dependencyIdentifier = status.dependencyIdentifier
                if (dependencyIdentifier != null && dependencyIdentifier == newValue.identifier && status.state.get() == ResourceOperationState.WAITING_DEPENDENCY) {
                    logger.info("Bumping priority on ${status.arg.resource.resourceTypeName}[${status.arg.resource.uuid}].${status.f.name} due to relevant update on $key")

                    status.state.compareAndExchangePush(
                        ResourceOperationState.WAITING_DEPENDENCY,
                        ResourceOperationState.WAITING_DEPENDENCY_EXPECTED_SATISFIED
                    )
                }
            }
        }
    }

    fun getOperationStatusInfo(workflowHandleId: String): WorkflowOperationStatusInfo {
        val frozenResourceOperations: List<ResourceOperationStatus<T, P, ForeignKey>> = resourceOperations
            .flatMap { (_, operations) ->
                operations.filter {
                    it.arg.volumeState.workflowHandleId == workflowHandleId
                }
            }

        // Fname -> List[waiting dependency display names]
        val pendingOperations = frozenResourceOperations
            .filter { !it.state.get().isFinished() }
            .groupBy {
                it.f.name
            }
            .mapValues {
                it.value.map { s ->
                    s.dependencyIdentifier?.let { identifier ->
                        "${identifier.resourceKey.name}[${identifier.resourceUuid}].${identifier.propertyKey.name}"
                    }
                        ?: s.blockedBy
                        ?: s.state.get().name
                }
            }

        // Fname -> List[uuid]
        val inFlightResourceOperations = frozenResourceOperations
            .filter { it.state.get() == ResourceOperationState.INFLIGHT }
        val inFlightOperations = inFlightResourceOperations
            .groupBy {
                it.f.name
            }
            .mapValues {
                it.value.map { e -> e.arg.resource.uuid }
            }

        val inFlightOperationCount = inFlightResourceOperations.size

        val completedResourceOperations = frozenResourceOperations
            .filter {
                val currentState = it.state.get()

                // Note includes failed
                currentState == ResourceOperationState.REDUNDANT
                        || currentState == ResourceOperationState.SUCCEEDED
                        || currentState == ResourceOperationState.FAILED
            }
        val completedOperationCount = completedResourceOperations
            .size
        val completedOperations = completedResourceOperations
            .groupBy {
                it.f.name
            }
            .mapValues {
                it.value.map { s -> s.state.get().name }
            }

        return WorkflowOperationStatusInfo(
            frozenResourceOperations,
            pendingOperations,
            inFlightOperationCount,
            inFlightOperations,
            completedOperationCount,
            completedOperations,
        )
    }

    @Synchronized
    fun arbitrate(
        workflowResourceModel: WorkflowResourceModel,
    ): Mono<Void> {
        val workflowHandleId = workflowResourceModel.workflowHandleId

        var newMono: Mono<Void>? = null
        arbitrateProcessLockMap.computeIfAbsent(workflowHandleId) {
            val thisNewMono = Mono.defer {
                val operandResources = resourceOperations.flatMap { (_, resources) ->
                    resources.map { it.arg.resource }
                }
                val storedResources = resourceStore.values
                val allResources = (operandResources + storedResources).distinctBy { it.uuid }

                // Decide resource update acceptance in parallel
                Flux.fromIterable(allResources).flatMap({ resource ->
                    arbiter.processPendingProposals(
                        this,
                        workflowResourceModel,
                        resource
                    )
                }, 16).collectList()
            }
                .doOnTerminate {
                    arbitrateProcessLockMap.remove(workflowHandleId)
                }
                .doOnCancel {
                    arbitrateProcessLockMap.remove(workflowHandleId)
                }
                .then()

            newMono = thisNewMono

            thisNewMono
        }

        return newMono ?: Mono.empty()
    }

    @Synchronized
    private fun addResource(
        workflowId: Long,
        resource: T,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
        contextView: ContextView,
    ): Boolean {
        val prevResource = resourceStore.putIfAbsent(resource.uuid, resource)
        if (prevResource != null) {
            return false
        }
        resourceLogger.info("$resourceDisplayName ${resource.uuid} REGISTER")

        val opArgs = unaryFunctions
            .map {
                ResourceOperationStatus(
                    it,
                    UpdateArgument(workflowId, resource, volumeState, artifactSink, contextView, maxEdgeRetries, null),
                    AtomicStack(
                        ResourceOperationState.NEW,
                        logId = "$resourceDisplayName[${resource.uuid}].${it.name}.state"
                    ),
                    resourceAcquireAttempts = 0,
                    blockedBy = null,
                    dependencyIdentifier = null,
                )
            }

        val prevOps = resourceOperations.putIfAbsent(resource.uuid, opArgs)

        if (prevOps == null) {
            for ((k, _) in opArgs) {
                resourceLogger.info("$resourceDisplayName ${resource.uuid} ADD ${k.name}")
            }
        }
        return true
    }

    @Synchronized
    private fun reAddOperation(
        uuid: String,
        status: ResourceOperationStatus<T, P, ForeignKey>,
        exception: Throwable,
    ) {
        resourceLogger.info("$resourceDisplayName $uuid RE-ADD ${status.f.name}")

        val nextContextView =
            Context.of(status.arg.contextView)
                .put(ALLOW_CACHED_COMPLETIONS, false)
        val nextStatus = status.copy(
            arg = status.arg.copy(
                contextView = nextContextView,
                retriesRemaining = status.arg.retriesRemaining - 1,
                lastException = exception,
            )
        )

        resourceOperations.compute(uuid) { _, l ->
            (l ?: emptyList()).filter {
                it.f.name != nextStatus.f.name
            } + nextStatus
        }
    }

    @Synchronized
    private fun updateStatus(
        uuid: String,
        newStatus: ResourceOperationStatus<T, P, ForeignKey>,
    ) {
        resourceOperations.compute(uuid) { _, l ->
            (l ?: emptyList()).filter {
                it.f.name != newStatus.f.name
            } + newStatus
        }
    }

    fun acquirePendingOperations(
        maxNumOperations: Int? = null,
    ): List<ResourceOperationStatus<T, P, ForeignKey>> {
        var numAcquired = 0
        return resourceOperations
            .flatMap {
                it.value.filter { status ->
                    if (maxNumOperations != null && numAcquired >= maxNumOperations) {
                        false
                    } else {
                        // For each ready state, try a conditional check and set to effectively acquire a processing lock
                        // on the resource
                        // Start with a soft check to avoid many check-and-sets, since worst case we skip an op that could
                        // be processed
                        val currentState = status.state.get()
                        val acquired = if (currentState.isPending()) {
                            status.state.compareAndExchangePush(currentState, ResourceOperationState.PROCESSING)
                                .also { didSet ->
                                    if (!didSet) {
                                        logger.error("Operation state changed while acquiring for processing - skipping")
                                    }
                                }
                        } else {
                            false
                        }

                        if (acquired) {
                            val logKey = "${status.f.name}:${status.arg.resource.uuid}"
                            logger.debug("$logKey acquired - processing")
                            numAcquired++
                        }

                        acquired
                    }

                }
            }
            .sortedBy {
                // Give preference to operations with fewer attempts
                it.resourceAcquireAttempts
            }
    }


    private fun acquireAndPerformOperation(
        status: ResourceOperationStatus<T, P, ForeignKey>,
    ): Mono<Void> {
        return Mono.defer {
            val f = status.f
            val arg = status.arg

            val (workflowId, resource, volumeState, artifactSink, _) = arg

            cancellationService.workflowIsCancelled(workflowId)
                .flatMap { isCancelled ->
                    if (isCancelled) {
                        resourceLogger.info("$resourceDisplayName ${resource.uuid} CANCEL ${f.name}")
                        Mono.empty()
                    } else if (arg.retriesRemaining <= 0) {
                        Mono.error(OperationRetriesExhaustedException(f.name, resource.uuid, arg.lastException))
                    } else {
                        val operationKey = "${f.name}:${arg.resource.uuid}"
                        propertyValueLockManager
                            .withAttempt(operationKey) { propertyValueLocker ->
                                f.prepareAndApplyUpdate(
                                    resource,
                                    volumeState,
                                    artifactSink,
                                    propertyValueLocker,
                                ).flatMap { operationSupplier ->
                                    logger.info("$operationKey got prepared operation")
                                    logger.debug("Will perform ${f.name}:${arg.resource.uuid}")

                                    performPreparedOperation(
                                        status,
                                        operationSupplier,
                                        propertyValueLocker,
                                    )
                                                                                .onErrorResume { exception ->
                                            handleProcessingException(status, exception)
                                                .then()
                                        }
                                }
                                    .onErrorResume { ex ->
                                        handleProcessingException(status, ex)
                                            .then()
                                    }
                            }
                    }
                }
        }
    }

    private fun performPreparedOperation(
        status: ResourceOperationStatus<T, P, ForeignKey>,
        operationSupplier: OperationSupplier,
        propertyValueLocker: PropertyValueLocker,
    ): Mono<Void> {
        val (_, resource, volumeState, artifactSink, contextView) = status.arg
        val f = status.f
        val arg = status.arg
        val uuid = resource.uuid

        // Here we know the conditions are satisfied for the operation
        val logKey = "${status.f.name}:${arg.resource.uuid}"
        logger.debug("$logKey ready - inflight")
        status.state.push(ResourceOperationState.INFLIGHT)

        // Perform greedily to unblock dependents
        return Mono.defer {
            logger.info("Attempting to perform operation ${resourceDisplayName}[$uuid].${f.name}")

            operationSupplier()
                .cache()
                .contextWrite(contextView)
                .subscribeOn(scheduler)
                .publishOn(postProcessor)
                .then(Mono.defer<Void> {
                    try {
                        f.checkPostConditions(
                            resource,
                            volumeState,
                            artifactSink,
                        )
                        resourceLogger.info("$resourceDisplayName $uuid POSTPASS ${f.name}")

                        logger.info("Operation succeeded: ${resourceDisplayName}[$uuid].${f.name}")
                        status.state.push(ResourceOperationState.SUCCEEDED)

                        Mono.empty()
                    } catch (e: Exception) {
                        logger.error(
                            "Failed post-condition for $resourceDisplayName ${f.name} $uuid",
                            e
                        )
                        resourceLogger.info("$resourceDisplayName $uuid POSTFAIL ${f.name}")

                        if (e is PostConditionFailedException) {
                            Mono.error(e)
                        } else {
                            Mono.error(
                                PostConditionFailedException(
                                    message = "Post-condition failed: ${e.message}",
                                    cause = e
                                )
                            )
                        }
                    }
                })
                .onErrorResume { exception ->
                    logger.error("Operation failed: ${resourceDisplayName}[$uuid].${f.name}", exception)
                    handleProcessingException(status, exception)
                        .then()
                }
                .doOnTerminate {
                    propertyValueLocker.releaseAll()
                }
                .doOnCancel {
                    propertyValueLocker.releaseAll()
                }
                .subscribe()
                .toMono()
        }.then()
    }

    private fun handleProcessingException(
        status: ResourceOperationStatus<T, P, ForeignKey>,
        exception: Throwable,
    ): Mono<ResourceOperationState> {
        val uuid = status.arg.resource.uuid
        val logKey =
            "${status.f.name}:$uuid"

        return when (exception) {
            // Incl. RequiredLockAcquireException etc
            is RequiredValueMissingBaseException -> when (exception) {
                is RequiredProposedValueMissingException,
                is RequiredValueMissingException -> {
                    logger.debug("$logKey required value missing - waiting\n${exception::class.java}\n${exception.shortName}")

                    val didSwap = status.state.popAndPushConditionally(
                        ResourceOperationState.PROCESSING,
                        ResourceOperationState.WAITING_DEPENDENCY
                    )

                    if (didSwap) {
                        val newStatus = status.copy(
                            resourceAcquireAttempts = status.resourceAcquireAttempts + 1,
                            blockedBy = exception.shortName,
                            dependencyIdentifier = exception.dependencyIdentifier,
                        )
                        updateStatus(uuid, newStatus)
                    }

                    // TODO: better communicate case where status is not conditionally updated
                    Mono.just(
                        ResourceOperationState.WAITING_DEPENDENCY
                    )
                }

                is RequiredResourceMissingException -> {
                    logger.debug("$logKey required resource missing - waiting")

                    val didSwap = status.state.popAndPushConditionally(
                        ResourceOperationState.PROCESSING,
                        ResourceOperationState.WAITING_DEPENDENCY
                    )

                    if (didSwap) {
                        updateStatus(
                            uuid, status.copy(
                                resourceAcquireAttempts = status.resourceAcquireAttempts + 1,
                                blockedBy = exception.shortName,
                                dependencyIdentifier = exception.dependencyIdentifier,
                            )
                        )
                    }

                    Mono.just(ResourceOperationState.WAITING_DEPENDENCY)
                }

                is RequiredLockAcquireException -> {
                    logger.debug("$logKey lock not acquired - waiting\n${exception::class.java}\n${exception.shortName}")

                    val didSwap = status.state.popAndPushConditionally(
                        ResourceOperationState.PROCESSING,
                        ResourceOperationState.WAITING_LOCK
                    )

                    if (didSwap) {
                        val newStatus = status.copy(
                            blockedBy = exception.shortName,
                        )
                        updateStatus(uuid, newStatus)
                    }

                    Mono.just(
                        ResourceOperationState.WAITING_LOCK
                    )
                }

                is RequiredLockedResourceRenderException -> {
                    logger.debug("$logKey unresolved proposals - waiting\n${exception::class.java}\n${exception.shortName}")

                    val didSwap = status.state.popAndPushConditionally(
                        ResourceOperationState.PROCESSING,
                        ResourceOperationState.WAITING_ARBITRATION
                    )

                    if (didSwap) {
                        val newStatus = status.copy(
                            blockedBy = exception.shortName,
                        )
                        updateStatus(uuid, newStatus)
                    }

                    Mono.just(
                        ResourceOperationState.WAITING_ARBITRATION
                    )
                }
            }

            is OperationCompleteException -> {
                // Do nothing
                logger.debug("$logKey operation complete - redundant")
                status.state.push(ResourceOperationState.REDUNDANT)
                resourceLogger.info("$resourceDisplayName $uuid DONE ${status.f.name}")
                Mono.just(ResourceOperationState.REDUNDANT)
            }

            is OperationRetriesExhaustedException -> {
                // Fail
                // TODO: Remediation
                logger.debug("$logKey retries exhausted - failed")
                status.state.push(ResourceOperationState.FAILED)
                logger.error("PERMANENTLY FAILED EDGE: ${status.f.name}", exception)
                resourceLogger.info("$resourceDisplayName $uuid ULTFAIL ${status.f.name}")
                Mono.just(ResourceOperationState.FAILED)
            }

            is PostConditionFailedException -> {
                // Re-add with one fewer retry
                logger.warn("Re-adding edge ${status.f.name} to queue on error", exception)
                logger.debug("$logKey post condition failed - retry")
                status.state.push(ResourceOperationState.RETRY)

                @Suppress("UNCHECKED_CAST")
                reAddOperation(
                    uuid,
                    status as ResourceOperationStatus<T, P, ForeignKey>,
                    exception,
                )
                Mono.just(ResourceOperationState.RETRY)
            }

            else -> {
                // Unknown error - bad but need to handle it
                logger.error("Encountered unrecognized error type during operation", exception)
                logger.debug("$logKey unknown error - retry")
                status.state.push(ResourceOperationState.RETRY)
                resourceLogger.info("$resourceDisplayName $uuid ERR ${status.f.name}")

                // Re-add with one fewer retry
                @Suppress("UNCHECKED_CAST")
                reAddOperation(
                    uuid,
                    status as ResourceOperationStatus<T, P, ForeignKey>,
                    exception,
                )
                Mono.just(ResourceOperationState.RETRY)
            }
        }
    }

    fun processSingleResourceOperation(
        acquiredOperationStatus: ResourceOperationStatus<T, P, ForeignKey>,
    ): Mono<ResourceOperationState> {
        fun setIfNeeded(): Boolean {
            // Catch-all reset to waiting if no more granular state change happens
            // Maybe consider reverting
            return if (acquiredOperationStatus.state.get() == ResourceOperationState.PROCESSING) {
                acquiredOperationStatus.state.compareAndExchangePush(
                    ResourceOperationState.PROCESSING,
                    ResourceOperationState.WAITING
                )
            } else {
                return false
            }
        }

        return acquireAndPerformOperation(acquiredOperationStatus)
                        .doOnSuccess {
                if (setIfNeeded()) {
                    logger.warn("${acquiredOperationStatus.f.name}:${acquiredOperationStatus.arg.resource.uuid} did reset on success")
                }
            }
            .doOnError {
                if (setIfNeeded()) {
                    logger.warn("${acquiredOperationStatus.f.name}:${acquiredOperationStatus.arg.resource.uuid} did reset on error - ${it::class.java.simpleName}")
                }
            }
            .doOnCancel {
                if (setIfNeeded()) {
                    logger.warn("${acquiredOperationStatus.f.name}:${acquiredOperationStatus.arg.resource.uuid} did reset on cancel")
                }
            }
            .then(nonBlocking {
                acquiredOperationStatus.state.get()
            })
    }

    fun getInFlightOperationCounts(): Map<String, Int> {
        // Count by workflowHandleId
        return resourceOperations
            .flatMap {
                it.value
            }
            .groupBy {
                it.arg.volumeState.workflowHandleId
            }
            .mapValues { (_, ops) ->
                ops
                    .count { it.state.get() == ResourceOperationState.INFLIGHT }
            }
    }

    fun registerResource(
        volumeState: VolumeState,
        workflowResourceModel: WorkflowResourceModel,
        streamingResource: T,
        artifactSink: FluxSink<Artifact>
    ): Mono<Boolean> {
        return Mono.deferContextual { contextView ->
            Mono.fromCallable {
                addResource(
                    workflowResourceModel.workflowHandleId.toLong(),
                    streamingResource,
                    volumeState,
                    artifactSink,
                    contextView
                )
            }
        }
    }

    private fun hasStrongPathToProjectOwner(
        resource: ObjectModelResource<*, *, *>,
    ): Boolean {
        return lifecycleService.hasStrongPathToProjectOwner(resource)
    }

    @Synchronized
    fun pruneDetachedResources(allAttachedUuids: Set<String>) {
        resourceOperations
            .filter {
                it.key !in allAttachedUuids
            }
            .forEach { (uuid, ops) ->
                ops.forEach { op ->
                    val oldState = op.state.get()
                    if (oldState != ResourceOperationState.REDUNDANT) {
                        // TODO: Limit redundant work
                        if (hasStrongPathToProjectOwner(op.arg.resource)) {
                            logger.info("Not pruning ${op.f.name}:$uuid in state ${oldState.name} due to existent path to root")
                        } else {
                            resourceLogger.info("$resourceDisplayName $uuid PRUNE_${oldState.name}")
                            logger.debug("Pruning $resourceDisplayName[$uuid].${op.f.name}")
                            op.state.push(ResourceOperationState.REDUNDANT)
                        }
                    }
                }
            }
    }

    fun reprocessCompletedResourcesFailingPostConditions(workflowHandleId: String): Int {
        // Temporarily disabled for debugging - TODO: Re-enable
        return 0

//        val processedResources = processedResources.entries.toList()
//
//        var processed = 0
//        for ((uuid, ops) in processedResources) {
//            for ((f, arg) in ops) {
//                val (projectTask, resource, volumeState, artifactSink, _) = arg
//                val opWorkflowHandleId = volumeState.workflowHandleId
//                if (workflowHandleId != opWorkflowHandleId) {
//                    continue
//                }
//
//                val satisfiesPostConditions = try {
//                    f.checkPostConditions(
//                        projectTask,
//                        resource,
//                        volumeState,
//                        artifactSink,
//                    )
//                    true
//                } catch (e: Exception) {
//                    false
//                }
//
//                if (!satisfiesPostConditions) {
//                    logger.debug("Re-processing ${f.name} on $uuid")
//                    reAddOperation(uuid, f, arg)
//
//                    processed++
//                }
//            }
//        }
//
//        return processed
    }

    fun reprocessCompletedResources(workflowHandleId: String): Int {
        // Temporarily disabled for debugging - TODO: Re-enable
        return 0

//        val processedResources = processedResources.entries.toList()
//
//        var processed = 0
//        for ((uuid, ops) in processedResources) {
//            for ((f, arg) in ops) {
//                val (_, _, volumeState, _, _) = arg
//                val opWorkflowHandleId = volumeState.workflowHandleId
//                if (workflowHandleId != opWorkflowHandleId) {
//                    continue
//                }
//
//                logger.debug("Re-processing ${f.name} on $uuid")
//                reAddOperation(uuid, f, arg)
//
//                processed++
//            }
//        }
//
//        return processed
    }

    companion object {
        private const val ALLOW_CACHED_COMPLETIONS = "allow_cached_completions"

        private val logger = LoggerFactory.getLogger(WorkflowSingleResourceProcessor::class.java)

        private val scheduler = Schedulers.newBoundedElastic(
            8,
            100_000,
            "op-processor",
        )

        private val postProcessor = Schedulers.newBoundedElastic(
            4,
            100_000,
            "post-processor",
        )
    }
}
