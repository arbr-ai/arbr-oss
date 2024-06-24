package com.arbr.engine.services.workflow.state

import com.arbr.api.workflow.core.WorkflowStatus
import com.arbr.engine.services.db.client.ApplicationCompletionStore
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.store.ResourceKVStoreProvider
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.og_engine.artifact.StatusArtifact
import com.arbr.og_engine.core.*
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
import com.arbr.util_common.LexIntSequence
import com.arbr.util_common.invariants.Invariants
import com.arbr.util_common.reactor.nonBlocking
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.system.exitProcess

/**
 * TODO: Refactor and simplify
 */
@Component
class WorkflowResourcePublisher(
    private val workflowResourceManager: WorkflowResourceManager,
    private val workflowStateService: WorkflowStateService,
    private val applicationCompletionStore: ApplicationCompletionStore,

    private val resourceKVStoreProviders: List<ResourceKVStoreProvider<*>>,
    private val workflowSingleResourceProcessors: List<WorkflowSingleResourceProcessor<*, *, *>>,
    private val mapper: ObjectMapper,
) {
    private val pollInProgress = AtomicBoolean(false)

    // Workflow handle ID -> count
    private val consecutiveRoundsWithoutChange = ConcurrentHashMap<String, Int>()

    // Workflow handle ID -> Count map
    private val lastOperationStatusMaps = ConcurrentHashMap<String, Map<String, WorkflowOperationStatusInfo>>()

    private fun failWorkflow(workflowHandleId: String, message: String) = workflowStateService.putWorkflowArtifact(
        workflowHandleId, StatusArtifact(
            WorkflowStatus.FAILED,
            message,
            Exception(message),
        )
    ).then(
        workflowResourceManager.cancelWorkflow(workflowHandleId)
    ).then()

    private fun getOperationCountMap(workflowHandleId: String): Map<String, WorkflowOperationStatusInfo> {
        return workflowSingleResourceProcessors.associate {
            it.resourceDisplayName to it.getOperationStatusInfo(workflowHandleId)
        }
    }

    private fun debugAnalyzeDependencies(
        workflowHandleId: String,
        newOperationStatusInfoMap: Map<String, WorkflowOperationStatusInfo>,
    ) {
        val resourceOperations = newOperationStatusInfoMap.flatMap { it.value.resourceOperations }

        statusLogger.info("Analyzing stall of workflow $workflowHandleId on ${resourceOperations.size} operations...")

        val stateCounts = resourceOperations
            .groupBy { it.state.get() }
            .mapValues { it.value.size }
            .toList()
            .sortedBy { it.first.ordinal }

        statusLogger.info("State counts:\n" + stateCounts.joinToString("\n") { it.first.name + ": " + it.second })

        statusLogger.info("Incomplete operations:")
        resourceOperations.forEach { resourceOperationStatus ->
            val state = resourceOperationStatus.state.get()
            if (state.isPending()) {
                val operand = resourceOperationStatus.arg.resource
                statusLogger.info("=".repeat(50))
                statusLogger.info("Operation ${resourceOperationStatus.f.name} on ${operand.resourceTypeName}[${operand.uuid}]")
                statusLogger.info("Most recent observed state: $state")
                statusLogger.info("Prior observed state: ${resourceOperationStatus.state.getPriorValue()}")
                statusLogger.info("Acquire attempts: ${resourceOperationStatus.resourceAcquireAttempts}")
                statusLogger.info("Blocker tag: ${resourceOperationStatus.blockedBy}")

                val pvsString = resourceOperationStatus.dependencyIdentifier?.let {
                    "${it.resourceKey.name}[${it.resourceUuid}].${it.propertyKey.name}"
                }
                statusLogger.info("Dependency PVS ID: $pvsString")

                statusLogger.info("=".repeat(50))
            }
        }

    }

    private fun checkOperationCountsUpdated(
        workflowHandleId: String,
        newOperationStatusInfoMap: Map<String, WorkflowOperationStatusInfo>,
    ) {
        val oldOperationStatusInfoMap = lastOperationStatusMaps[workflowHandleId]
            ?: return

        statusLogger.info("Resource stats for workflow $workflowHandleId")

        fun resourceStatusLogKey(s: ResourceOperationStatus<*, *, *>): String {
            return s.dependencyIdentifier?.let { identifier ->
                "${identifier.resourceKey.name}[${identifier.resourceUuid}].${identifier.propertyKey.name}"
            }
                ?: s.blockedBy
                ?: s.state.get().name
        }

        var totalInFlightCount = 0
        var completelyUnchanged = true
        for ((resourceTypeName, newInfo) in newOperationStatusInfoMap.entries) {
            val oldOperationStatusInfo = oldOperationStatusInfoMap[resourceTypeName]!!
            val oldMap: Map<String, List<ResourceOperationStatus<*, *, *>>> = oldOperationStatusInfo.resourceOperations
                .filter { !it.state.get().isFinished() }
                .groupBy { it.f.name }
            val oldCompleted = oldOperationStatusInfo.completedOperationCount
            val oldInFlight = oldOperationStatusInfo.inFlightOperationCount

            val newMap: Map<String, List<ResourceOperationStatus<*, *, *>>> = newInfo.resourceOperations
                .filter { !it.state.get().isFinished() }
                .groupBy { it.f.name }
            val newCompleted = newInfo.completedOperationCount
            val newInFlight = newInfo.inFlightOperationCount

            totalInFlightCount += newInFlight

            if (oldCompleted != newCompleted || oldInFlight != newInFlight) {
                completelyUnchanged = false
            }

            if (newInFlight > 0) {
                statusLogger.info(
                    "In-flight $resourceTypeName operations: $newInFlight\n${
                        newInfo.inFlightOperations.entries.joinToString(
                            "\n"
                        ) { it.key + ": " + it.value }
                    }"
                )

                // Don't count unchanged rounds while there are in-flight ops
                completelyUnchanged = false
            }

            val pendingLogString = oldMap.keys.union(newMap.keys).joinToString("\n") { fn ->
                val oldList = oldMap.getOrDefault(fn, emptyList())
                val newList = newMap.getOrDefault(fn, emptyList())
                if (oldList.size != newList.size) {
                    completelyUnchanged = false
                }

                newList.joinToString("\n") {
                    "$resourceTypeName[${it.arg.resource.uuid}].$fn <- ${resourceStatusLogKey(it)}"
                }
            }
            if (pendingLogString.isNotBlank()) {
                statusLogger.info("Pending:\n$pendingLogString")
            }
        }

        val completedLogString = StringBuilder().also { sb ->
            sb.append("Completed ")

            for ((_, newInfo) in newOperationStatusInfoMap.entries) {
                val newCompletedMap = newInfo.completedOperations

                for ((fn, newList) in newCompletedMap) {
                    val counts = newList.groupBy { it }.mapValues { it.value.count() }
                        .filterValues { it > 0 }
                    if (counts.isNotEmpty()) {
                        val functionString = "$fn: ${counts.entries.joinToString(", ") { "${it.value} x ${it.key}" }}; "
                        sb.append(functionString)
                    }
                }
            }
        }.toString()
        statusLogger.info(completedLogString)

        if (completelyUnchanged) {
            val newCount = consecutiveRoundsWithoutChange.compute(workflowHandleId) { _, c ->
                (c ?: 0) + 1
            } ?: 0

            statusLogger.info("Consecutive rounds without status change: $newCount")

            Invariants.check { require ->
                val maxRounds = 8
                if (newCount > maxRounds) {
                    debugAnalyzeDependencies(
                        workflowHandleId,
                        newOperationStatusInfoMap,
                    )
                }

                require(newCount <= maxRounds)
            }

            // Delay checks if there are in-flight operations in case they're just slow

            // Temporarily disabled - TODO: Re-enable

//            val interval = if (totalInFlightCount == 0) 8 else 24
//
//            if (newCount > 0 && newCount % interval == 0) {
//                logger.info("Reprocessing post condition failures...")
//
//                val postConditionFailuresProcessed = workflowSingleResourceProcessors.sumOf {
//                    it.reprocessCompletedResourcesFailingPostConditions(workflowHandleId)
//                }
//
//                logger.info("Processed $postConditionFailuresProcessed post condition failures")
//
//                // While post condition failures may happen because of intermittent failures, missing dependencies due
//                // to something else means a dependency is not expressed in post-conditions
//                if (postConditionFailuresProcessed == 0) {
//                    logger.error("REPROCESSING ALL!!! THIS INDICATES A PROGRAMMING ERROR!")
//                    val totalResourcesProcessed = workflowSingleResourceProcessors.sumOf {
//                        it.reprocessCompletedResources(workflowHandleId)
//                    }
//
//                    if (totalResourcesProcessed == 0) {
//                        logger.error("Complete failure: No remaining resources to reprocess")
//                        failWorkflow(workflowHandleId, "Complete failure: No remaining resources to reprocess")
//                            .subscribeOn(Schedulers.boundedElastic())
//                            .subscribe()
//                    }
//                }
//            }
        } else {
            consecutiveRoundsWithoutChange[workflowHandleId] = 0
        }
    }

    /*
    Match leniently against:
        gpt-4-0613
        gpt-3.5-turbo-16k-0613
        gpt-3.5-0613
     */
    private fun matchModelForConsumption(
        usedModelName: String
    ): OpenAiChatCompletionModel? {
        return if (usedModelName.lowercase().startsWith("gpt-4")) {
            OpenAiChatCompletionModel.GPT_4_0613
        } else if (usedModelName.lowercase().let { it.startsWith("gpt-3.5") && it.contains("16k") }) {
            OpenAiChatCompletionModel.GPT_3_5_TURBO_16K_0613
        } else if (usedModelName.lowercase().let { it.startsWith("gpt-3.5") && !it.contains("16k") }) {
            OpenAiChatCompletionModel.GPT_3_5_TURBO_0613
        } else {
            null
        }
    }

    private fun checkTotalWorkflowConsumption(
        workflowHandleIds: List<String>,
    ): Mono<Map<String, WorkflowConsumption>> {
        val dbWorkflowIds = workflowHandleIds.map { it.toLong() }
        return applicationCompletionStore
            .getTotalUsageByWorkflowHandleId(dbWorkflowIds)
            .map { rawConsumptionList ->
                rawConsumptionList.groupBy { it.workflowId.toString() }
                    .mapValues { (_, rawConsumptionListForWorkflow) ->
                        val tokensConsumedByModel = rawConsumptionListForWorkflow.groupBy { it.usedModel }
                            .toList()
                            .associate { (usedModelName, rc) -> matchModelForConsumption(usedModelName) to rc.sumOf { it.totalTokens } }

                        /*
                        Match leniently against:
                            gpt-4-0613
                            gpt-3.5-turbo-16k-0613
                            gpt-3.5-0613
                         */
                        WorkflowConsumption(
                            tokensConsumedByModel[OpenAiChatCompletionModel.GPT_4_0613] ?: 0,
                            tokensConsumedByModel[OpenAiChatCompletionModel.GPT_3_5_TURBO_16K_0613] ?: 0,
                            tokensConsumedByModel[OpenAiChatCompletionModel.GPT_3_5_TURBO_0613] ?: 0,
                            tokensConsumedByModel[null] ?: 0,
                        )
                    }
            }
    }

    private fun checkForCancelledWorkflows(
        workflowResourceModels: Map<String, WorkflowResourceModel>,
    ): Mono<Void> {
        return Flux.fromIterable(workflowResourceModels.keys).flatMap { workflowResourceHandleId ->
            workflowStateService.getWorkflowState(workflowResourceHandleId).flatMap { workflowState ->
                if (workflowState.status == WorkflowStatus.CANCELLED) {
                    workflowResourceManager.cancelWorkflow(workflowResourceHandleId)
                } else {
                    Mono.empty()
                }
            }
        }.then()
    }

    private fun checkConsumptionLimit(workflowHandleId: String, consumption: WorkflowConsumption): Mono<Void> {
        // Just check total consumption for now
        val totalConsumption =
            consumption.gpt4Usage + consumption.gpt35t16kUsage + consumption.gpt35tUsage + consumption.unknownUsage
        return if (totalConsumption > workflowTokenUsageHardCap) {
            statusLogger.error("Workflow $workflowHandleId exceeded total consumption limit of $workflowTokenUsageHardCap - terminating")
            val message = "Exceeded token consumption limit"
            failWorkflow(workflowHandleId, message)
        } else {
            Mono.empty()
        }
    }

    @Synchronized
    private fun innerPollResourceStatus(): Mono<Void> {
        return workflowResourceManager.getWorkflowResourceModels().flatMap { resourceModels ->
            for (workflowHandleId in resourceModels.keys) {
                val statusInfoMap = getOperationCountMap(workflowHandleId)

                checkOperationCountsUpdated(workflowHandleId, statusInfoMap)
                lastOperationStatusMaps[workflowHandleId] = statusInfoMap
            }

            checkTotalWorkflowConsumption(resourceModels.keys.toList())
                .doOnNext {
                    for ((k, m) in it) {
                        statusLogger.info("Consumption for workflow $k: ${mapper.writeValueAsString(m)}")
                    }
                }
                .flatMap { consumptionMap ->
                    Flux.fromIterable(consumptionMap.entries).flatMap { (workflowHandleId, workflowConsumption) ->
                        checkConsumptionLimit(workflowHandleId, workflowConsumption)
                    }
                        .collectList()
                }
                .then(
                    checkForCancelledWorkflows(resourceModels)
                )
        }
    }

    private fun pruneDetachedResources(): Mono<Void>? {
        return workflowResourceManager.getWorkflowResourceModels().flatMap { resourceModels ->
            val zeroOpModels = resourceModels.filter { (id, _) ->
                workflowSingleResourceProcessors.sumOf { proc ->
                    val statusInfo = proc.getOperationStatusInfo(id)
                    statusInfo.inFlightOperationCount
                } == 0
            }.values

            if (zeroOpModels.isEmpty()) {
                return@flatMap Mono.empty()
            }

            // BFS for all UUIDs
            val allAttachedUuids = mutableSetOf<String>().also { attachedUuids ->
                var frontier: List<ObjectModelResource<*, *, *>> = resourceModels.values.flatMap {
                    listOf(
                        it.root
                    )
                }
                while (frontier.isNotEmpty()) {
                    attachedUuids.addAll(frontier.map { it.uuid })

                    val nextFrontier = frontier.flatMap {
                        it.getChildren().flatMap { (_, pvs) ->
                            // Include both accepted and proposed values to not prematurely prune
                            (pvs.getLatestValue()?.values ?: emptyList()) + (pvs.getLatestAcceptedValue()?.values
                                ?: emptyList())
                        } + it.getForeignKeys().flatMap { (_, pvs) ->
                            // Include single outbound FKs as well - note that this definitely introduces cycles, so the
                            // `allAttachedUuids` set is important
                            listOfNotNull(pvs.getLatestValue(), pvs.getLatestAcceptedValue())
                        }
                    }
                        .mapNotNull { it.resource() }
                        .distinctBy { it.uuid }
                        .filter { it.uuid !in attachedUuids }

                    frontier = nextFrontier
                }
            }


            // BFS again for strongly attached UUIDs, i.e. ones connected to the Project via parent keys
            val allStronglyAttachedUuids = mutableSetOf<String>().also { stronglyAttachedUuids ->
                var strongFrontier: List<ObjectModelResource<*, *, *>> = resourceModels.values.flatMap {
                    listOf(
                        it.root,
                    )
                }
                while (strongFrontier.isNotEmpty()) {
                    stronglyAttachedUuids.addAll(strongFrontier.map { it.uuid })

                    val nextFrontier = strongFrontier.flatMap {
                        it.getChildren().flatMap { (_, pvs) ->
                            // Include both accepted and proposed values to not prematurely prune
                            (pvs.getLatestValue()?.values ?: emptyList()) + (pvs.getLatestAcceptedValue()?.values
                                ?: emptyList())
                        }
                    }
                        .mapNotNull { it.resource() }
                        .distinctBy { it.uuid }
                        .filter { it.uuid !in stronglyAttachedUuids }

                    strongFrontier = nextFrontier
                }
            }

            /**
             * Working around type erasure problems when trying to bind FK to the object
             */
            fun <FK: NamedForeignKey> clearParentConditionallyForEachChild(
                objectModelResource: ObjectModelResource<*, *, FK>,
                oldUuid: String,
            ): List<Mono<Void>> {
                val updates = mutableListOf<Mono<Void>>()
                objectModelResource.getChildren().forEach { (fk, children) ->
                    val childList = children.getLatestValue()
                    childList?.forEach { (_, ref) ->
                        val child = ref.resourceErased()
                        val update = child?.setParentConditionally(fk, oldUuid, null)
                        if (update != null) {
                            updates.add(update)
                        }
                    }
                }
                return updates
            }

            val allUpdates = mutableListOf<Mono<Void>>()
            val weaklyAttachedUuids = allAttachedUuids - allStronglyAttachedUuids
            weaklyAttachedUuids.forEach { uuid ->
                resourceKVStoreProviders.forEach { provider ->
                    val kvStore = provider.provide()
                    val weaklyAttachedResource = kvStore[uuid]

                    if (weaklyAttachedResource != null) {
                        allUpdates.addAll(clearParentConditionallyForEachChild(weaklyAttachedResource, uuid))
                    }
                }
            }

            val updateMono = if (allUpdates.isNotEmpty()) {
                statusLogger.info("Performing ${allUpdates.size} child detachment cleanup operations")
                Flux.fromIterable(allUpdates)
                    .flatMap { it }
                    .collectList()
                    .then()
            } else {
                Mono.empty()
            }

            val pruneMono = Flux.fromIterable(workflowSingleResourceProcessors).concatMap { processor ->
                nonBlocking {
                    processor.pruneDetachedResources(allAttachedUuids)
                }
            }.then()

            updateMono
                .then(pruneMono)
        }
    }

    private fun performArbitration(): Mono<Void> {
        return workflowResourceManager.getWorkflowResourceModels().flatMap { resourceModels ->
            val zeroOpModels = resourceModels.filter { (id, _) ->
                workflowSingleResourceProcessors.sumOf { proc ->
                    val statusInfo = proc.getOperationStatusInfo(id)
                    statusInfo.inFlightOperationCount
                } == 0
            }.values

            Flux.fromIterable(zeroOpModels).concatMap { workflowResourceModel ->
                Flux.fromIterable(workflowSingleResourceProcessors).flatMap { processor ->
                    processor.arbitrate(workflowResourceModel)
                }
            }.then()
                        }
    }

    data class ProcessorAndStatus<T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>(
        val processor: WorkflowSingleResourceProcessor<T, P, ForeignKey>,
        val status: ResourceOperationStatus<T, P, ForeignKey>,
    ) {

        fun processSingleResourceOperation(): Mono<ResourceOperationState> {
            return processor.processSingleResourceOperation(status)
        }

        companion object {

            fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey> ofProcessor(
                processor: WorkflowSingleResourceProcessor<T, P, ForeignKey>
            ): List<ProcessorAndStatus<T, P, ForeignKey>> {
                return processor.acquirePendingOperations().map { ProcessorAndStatus(processor, it) }
            }

        }
    }

    /**
     * Sort by priority and limit length before handing off to processors
     */
    private fun sortAndTruncateResourceOperations(
        allResourceOperations: List<ProcessorAndStatus<*, *, *>>,
    ): Pair<List<ProcessorAndStatus<*, *, *>>, List<ProcessorAndStatus<*, *, *>>> {
        val maxOperationBatchSize = 32
        val sortedList = allResourceOperations
            .sortedBy {
                // TODO: Re-introduce opportunity to configure processor priority explicitly
                LexIntSequence(
                    listOf(
                        it.status.state.getPriorValue().ordinal,
                        it.status.resourceAcquireAttempts.toInt(),
                    )
                )
            }

        val head = mutableListOf<ProcessorAndStatus<*, *, *>>()
        val tail = mutableListOf<ProcessorAndStatus<*, *, *>>()
        for (ps in sortedList) {
            if (ps.status.state.getPriorValue() in listOf(
                    ResourceOperationState.NEW,
                    ResourceOperationState.WAITING_DEPENDENCY_EXPECTED_SATISFIED,
                    ResourceOperationState.RETRY
                )) {
                    head.add(ps)
                } else {
                tail.add(ps)
            }
        }

        return if (head.isEmpty() && tail.isNotEmpty()) {
            tail.take(maxOperationBatchSize) to (tail.drop(maxOperationBatchSize) + head)
        } else {
            head.take(maxOperationBatchSize) to (head.drop(maxOperationBatchSize) + tail)
        }
    }

    /**
     * Poll for updates to resource models via function application.
     * Returns operation statuses that were reported to have been processed.
     */
    private fun pollResourceUpdates(
        operationsToProcess: List<ProcessorAndStatus<*, *, *>>,
    ): Mono<List<ProcessorAndStatus<*, *, *>>> {
        val batchSize = 1

        Invariants.check { require ->
            val badStates = operationsToProcess.filter {
                it.status.state.get() != ResourceOperationState.PROCESSING
            }
            if (badStates.isNotEmpty()) {
                badStates.forEach { bs ->
                    statusLogger.info("${bs.status.f.name}:${bs.status.arg.resource.uuid} in unexpected state ${bs.status.state.get()}")
                }
            }

            require(
                operationsToProcess.all {
                    it.status.state.get() == ResourceOperationState.PROCESSING
                }
            )
        }

        fun processWindow(
            offset: Int,
            didEncounterOperationAwaitingArbitration: Boolean,
        ): Mono<Map<ResourceOperationState, List<ProcessorAndStatus<*, *, *>>>> {
            return if (offset >= operationsToProcess.size) {
                Mono.just(emptyMap())
            } else {
                val toIndex = min(operationsToProcess.size, offset + batchSize)
                val slice = operationsToProcess.slice(offset until toIndex)
                Flux.fromIterable(slice)
                    .concatMap { processorAndStatus ->
                        if (didEncounterOperationAwaitingArbitration) {
                            val revertedState =
                                processorAndStatus.status.state.popConditionally(ResourceOperationState.PROCESSING)
                            if (revertedState == null) {
                                val newFallbackState = ResourceOperationState.WAITING
                                val didSet = processorAndStatus.status.state.compareAndExchangePush(
                                    ResourceOperationState.PROCESSING,
                                    newFallbackState
                                )

                                if (didSet) {
                                    Mono.just(newFallbackState to processorAndStatus)
                                } else {
                                    // The operation is not processing - it must have been updated elsewhere, so just
                                    // return whatever state it's in
                                    Mono.just(processorAndStatus.status.state.get() to processorAndStatus)
                                }
                            } else {
                                Mono.just(revertedState to processorAndStatus)
                            }
                        } else if (processorAndStatus.status.state.get() == ResourceOperationState.PROCESSING) {
                            // Be sure to perform these acquisitions serially
                            val workflowHandleId = processorAndStatus.status.arg.volumeState.workflowHandleId
                            processorAndStatus.processSingleResourceOperation()
                                .map { newState ->
                                    newState to processorAndStatus
                                }
                                .onErrorResume(OperationRetriesExhaustedException::class.java) { ex ->
                                    failWorkflow(
                                        workflowHandleId,
                                        ex.message!!
                                    ).then(
                                        Mono.empty()
                                    )
                                }
                                .onErrorResume(Exception::class.java) { ex ->
                                    logger.error("Unrecognized error during workflow", ex)
                                    failWorkflow(
                                        workflowHandleId,
                                        ex.message ?: "Unknown error"
                                    ).then(
                                        Mono.empty()
                                    )
                                }
                        } else {
                            Mono.empty()
                        }
                    }
                    .collectList()
                    .flatMap { opStatePairs ->
                        val operationStateMap: Map<ResourceOperationState, List<ProcessorAndStatus<*, *, *>>> =
                            opStatePairs
                                .groupBy { it.first }
                                .mapValues { it.value.map { t -> t.second } }

                        val encounteredWaitingArbitration =
                            didEncounterOperationAwaitingArbitration || operationStateMap.getOrDefault(
                                ResourceOperationState.WAITING_ARBITRATION,
                                emptyList()
                            ).isNotEmpty()

                        opStatePairs.forEach { (_, processorAndStatus) ->
                            val stateValue = processorAndStatus.status.state.get()
                            if (stateValue in listOf(
                                    ResourceOperationState.WAITING_ARBITRATION,
                                    ResourceOperationState.WAITING_LOCK
                                )
                            ) {
                                // Temporary block state - revert to prior
                                processorAndStatus.status.state.popConditionally(stateValue)
                            }
                        }

                        processWindow(offset + batchSize, encounteredWaitingArbitration)
                            .defaultIfEmpty(emptyMap())
                            .map { innerMap ->
                                opStatePairs
                                    .also { il ->
                                        il.addAll(innerMap.flatMap { (s, l) -> l.map { s to it } })
                                    }
                                    .groupBy { it.first }
                                    .mapValues { it.value.map { t -> t.second } }
                            }
                    }
            }
        }

        return processWindow(0, false).flatMap { stateMap ->
            if (stateMap[ResourceOperationState.FAILED]?.isNotEmpty() == true) {
                Mono.error(stateMap[ResourceOperationState.FAILED]!!.first().status.arg.lastException!!)
            } else {
                Mono.just(stateMap.getOrDefault(ResourceOperationState.SUCCEEDED, emptyList()))
            }
        }
    }

    private fun pollResourceUpdatesWhileOperationsSucceed(
        round: Int = 0
    ): Mono<Void> {
        // Sort by priority and limit length before passing to processors
        val allResourceOperations = workflowSingleResourceProcessors
            .flatMap { processor ->
                ProcessorAndStatus.ofProcessor(processor)
            }
        val (operationsToProcess, unprocessedOperations) = sortAndTruncateResourceOperations(allResourceOperations)

        if (operationsToProcess.isEmpty()) {
            statusLogger.info("No operations to process")
            return Mono.empty()
        } else {
            statusLogger.info("Total operations to process: ${operationsToProcess.size}")
        }

        if (unprocessedOperations.isNotEmpty()) {
            statusLogger.info("Releasing ${unprocessedOperations.size} low priority operations")
            unprocessedOperations.forEach { op ->
                val didRevert = op.status.state.popConditionally(ResourceOperationState.PROCESSING)
                Invariants.check { require ->
                    require(didRevert != null)
                }
            }
        }

        return pollResourceUpdates(operationsToProcess)
                        .flatMap { processedOps ->
                if (processedOps.isEmpty()) {
                    statusLogger.info("Finishing at round $round")
                    Mono.empty()
                } else {
                    statusLogger.info("Got ${processedOps.size} updates at round $round")
                    performArbitration()
                        .then(pollResourceUpdatesWhileOperationsSucceed(round + 1))
                }
            }
    }

    private fun getPollOperations(
        pollRound: Int
    ): List<Mono<Void>> {
        return listOfNotNull(
            Mono.defer { pollResourceUpdatesWhileOperationsSucceed() },
            Mono.defer { performArbitration() },
            Mono.defer { pruneDetachedResources() ?: Mono.empty() },
            if (pollRound % 10 == 9) {
                Mono.defer { innerPollResourceStatus() }
            } else {
                Mono.empty()
            },
        )
    }

    private val pollSyncObject = Object()

    private val pollerRoundCounter = AtomicInteger()

    @Scheduled(fixedRate = 1093L)
    private fun poll() {
//        if (workflowResourceManager.getWorkflowResourceModels().isEmpty()) {
//            return
//        }

        val didSet = pollInProgress.compareAndSet(false, true)
        if (!didSet) {
            return
        }

        val round = pollerRoundCounter.incrementAndGet()

        statusLogger.info("Begin poller pass $round")

        val results = mutableListOf<Throwable>()

        val operationsMono = getPollOperations(round).withIndex()
            .map { (index, operation) ->
                operation
                    .doOnError { ex ->
                        statusLogger.error("Error during polling operation $index", ex)
                        results.add(ex)
                    }
            }
            .fold(Mono.empty<Void>()) { acc, mono -> acc.then(mono) }
            .doOnTerminate {
                val didFinish = pollInProgress.compareAndSet(true, false)
                if (didFinish) {
                    synchronized(pollSyncObject) {
                        pollSyncObject.notify()
                        statusLogger.info("Complete poller pass")
                    }
                }
            }
            .doOnCancel {
                val didFinish = pollInProgress.compareAndSet(true, false)
                if (didFinish) {
                    synchronized(pollSyncObject) {
                        pollSyncObject.notify()
                        statusLogger.info("Complete poller pass")
                    }
                }
            }
            .then()
            
        operationsMono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        synchronized(pollSyncObject) {
            try {
                pollSyncObject.wait(Duration.ofMinutes(60L).toMillis())
            } catch (e: InterruptedException) {
                statusLogger.warn("Polling process interrupted")
            }
        }

        if (results.isNotEmpty()) {
            statusLogger.error("Failing on ${results.first()}}")
            exitProcess(1)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowResourcePublisher::class.java)
        private val statusLogger = LoggerFactory.getLogger("com.topdown.WorkflowResourcePublisher")

        private const val workflowTokenUsageHardCap = 750_000L
    }
}