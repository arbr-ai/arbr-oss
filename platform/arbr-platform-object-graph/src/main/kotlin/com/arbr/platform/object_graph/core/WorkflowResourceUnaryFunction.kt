package com.arbr.platform.object_graph.core

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.requirements.DefaultRequirementsProvider
import com.arbr.og.object_model.common.requirements.RequirementsProvider
import com.arbr.og.object_model.common.requirements.RequirementsProviderWrapper
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.artifact.ProcessorStatusArtifact
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.platform.object_graph.alignable.PartialTrackingResourceAligner
import com.arbr.platform.object_graph.artifact.WorkflowProcessorStatus
import com.arbr.platform.object_graph.concurrency.LockAcquireException
import com.arbr.platform.object_graph.concurrency.LockedResourceRenderException
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.util_common.reactor.nonBlocking
import org.slf4j.LoggerFactory
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Instant

typealias OperationSupplier = () -> Mono<Void>

abstract class WorkflowResourceUnaryFunction<
        T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, // Listen
        T1 : ObjectModelResource<T1, P1, ForeignKey>, P1 : Partial<T1, P1, ForeignKey>, // Read root
        T2 : ObjectModelResource<T2, P2, ForeignKey>, P2 : Partial<T2, P2, ForeignKey>, // Write root
        ForeignKey: NamedForeignKey,
        >(
    private val objectModelParser: ObjectModelParser,
    private val requirementsProvider: RequirementsProvider = DefaultRequirementsProvider(),
): RequirementsProviderWrapper(requirementsProvider) {
    abstract val name: String

    abstract val targetResourceClass: Class<T1>
    abstract val writeTargetResourceClass: Class<T2>

    /**
     * Relies on the fact that dependency calculation is synchronous
     */
    private val propertyValueLocker: ThreadLocal<PropertyValueLocker>
        get() = requirementsProvider.propertyValueLocker

    /**
     * Get parents while limiting to mutually agreed-upon relationships
     * And a boolean indicating whether any parent FK values are yet undecided
     */
    private fun getReflexiveParents(
        resource: ObjectModelResource<*, *, *>,
    ): Pair<List<ObjectModelResource<*, *, *>>, Boolean> {
        var anyUndecided = false
        return resource.getForeignKeys().mapNotNull { (_, v) ->
            val parentRef = v.getLatestAcceptedValue()

            if (parentRef == null) {
                anyUndecided = true // Parent ref not yet set
                null
            } else {
                val parent = parentRef.resource()
                if (parent == null) {
                    null
                } else {
                    // Check any child set - TODO: Use actual FK relationship
                    val isChild = parent.getChildren().entries.any { (_, chl) ->
                        chl.getLatestAcceptedValue()?.any {
                            it.value.uuid == resource.uuid
                        } == true
                    }
                    if (isChild) {
                        parent
                    } else {
                        // Consider an unrequited relationship to be undecided since it could yet be resolved in favor
                        // of the child
                        anyUndecided = true

                        null
                    }
                }
            }
        } to anyUndecided
    }

    private fun <Q> findTargetResource(
        resources: List<ObjectModelResource<*, *, *>>,
        targetClass: Class<Q>,
    ): Q {
        val initialMatch = resources
            .filterIsInstance(targetClass)
            .firstOrNull()
        if (initialMatch != null) {
            return initialMatch
        }

        // BFS for a match
        val seen = resources.map { it.uuid }.toMutableSet()
        var anyUndecided = false
        var frontier = resources.flatMap { resource ->
            val (reflexiveParents, anyParentUndecided) = getReflexiveParents(resource)
            if (anyParentUndecided) {
                anyUndecided = true
            }
            reflexiveParents
        }
            .distinctBy { it.uuid }
        val maxRounds = 8

        var i = 0
        while (i < maxRounds && frontier.isNotEmpty()) {
            seen.addAll(frontier.map { it.uuid })

            val targetResources = frontier
                .filterIsInstance(targetClass)

            if (targetResources.size > 1) {
                val classes = resources.joinToString(", ") { it::class.java.toString() }
                logger.error("No unambiguous foreign key path to target resource $targetClass from $classes")
                throw Exception("No unambiguous foreign key path to target resource $targetClass from $classes")
            } else if (targetResources.isNotEmpty()) {
                return targetResources.first()
            }

            frontier = frontier
                .flatMap {
                    val (reflexiveParents, anyParentUndecided) = getReflexiveParents(it)
                    if (anyParentUndecided) {
                        anyUndecided = true
                    }
                    reflexiveParents
                }
                .filter { it.uuid !in seen }
                .distinctBy { it.uuid }
            i++
        }

        val classes = resources.joinToString(", ") { it::class.java.toString() }

        if (anyUndecided) {
            // Could conceivably achieve a path
            throw RequiredValueMissingException(
                targetClass,
                "No foreign key path to target resource $targetClass from $classes"
            )
        } else {
            logger.info("Completing ${resources.firstOrNull()?.resourceTypeName}:${resources.firstOrNull()?.uuid} due to inconceivable path to $targetClass")
            throw OperationCompleteException()
        }
    }

    /**
     * Optional for now: use the listen resource to select a particular read resource.
     * Throws: RequiredResourceMissingException, RequiredValueMissingException, OperationCompleteException
     */
    open fun selectReadResource(
        listenResource: T,
    ): T1 {
        return findTargetResource(listOf(listenResource), targetResourceClass)
    }

    /**
     * Optional for now: use the listen resource to select a particular write resource.
     * Throws: RequiredResourceMissingException, RequiredValueMissingException, OperationCompleteException
     */
    open fun selectWriteResource(
        listenResource: T,
    ): T2 {
        return findTargetResource(listOf(listenResource), writeTargetResourceClass)
    }

    private fun selectReadResourceAsync(
        listenResource: T,
    ): Mono<T1> {
        return nonBlocking {
            try {
                propertyValueLocker.set(PropertyValueLocker.noOpLocker)
                Pair(null, selectReadResource(listenResource))
            } catch (e: Exception) {
                Pair(e, null)
            }
        }.flatMap { (ex, q) ->
            if (q != null) {
                Mono.just(q)
            } else {
                Mono.error(ex ?: IllegalStateException())
            }
        }
    }

    private fun selectWriteResourceAsync(
        listenResource: T,
    ): Mono<T2> {
        return nonBlocking {
            try {
                propertyValueLocker.set(PropertyValueLocker.noOpLocker)
                Pair(null, selectWriteResource(listenResource))
            } catch (e: Exception) {
                Pair(e, null)
            }
        }.flatMap { (ex, q) ->
            if (q != null) {
                Mono.just(q)
            } else {
                Mono.error(ex ?: IllegalStateException())
            }
        }
    }

    open fun acquireWriteTargets(
        listenResource: T,
        readResource: T1,
        writeResource: T2,
        acquire: (ProposedValueReadStream<*>) -> Unit,
    ) {
        fun defaultAcquireWriteTargetsRec(
            objectModelResource: ObjectModelResource<*, *, *>,
        ) {
            objectModelResource.properties().values.forEach(acquire)

            objectModelResource.getChildren().values.forEach { fkStream ->
                if (fkStream.hasAnyUnresolvedProposals()) {
                    throw RequiredLockedResourceRenderException(
                        fkStream.identifier.resourceUuid,
                        fkStream.identifier.resourceKey.name
                    )
                }
                fkStream.getLatestAcceptedValue()?.values?.forEach { ref ->
                    ref.resource()?.let { childResource ->
                        defaultAcquireWriteTargetsRec(childResource)
                    }
                }
            }
        }

        defaultAcquireWriteTargetsRec(writeResource)
    }

    abstract fun prepareUpdate(
        listenResource: T,
        readResource: T1,
        writeResource: T2,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<T2, P2, ForeignKey>) -> Mono<Void>

    abstract fun checkPostConditions(
        listenResource: T,
        readTargetResource: T1,
        writeTargetResource: T2,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    )

    private fun prepareUpdateInnerAsync(
        listenResource: T,
        targetResource: T1,
        writeTargetResource: T2,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
        propertyValueLocker: PropertyValueLocker,
    ): Mono<(PartialObjectGraph<T2, P2, ForeignKey>) -> Mono<Void>> {
        return Mono.fromCallable {
            try {
                this.propertyValueLocker.set(propertyValueLocker)

                acquireWriteTargets(
                    listenResource,
                    readResource = targetResource,
                    writeTargetResource
                ) { pvs ->
                    propertyValueLocker.acquireWrite(pvs.identifier)
                }
                val innerResult = prepareUpdate(
                    listenResource,
                    targetResource,
                    writeTargetResource,
                    volumeState,
                    artifactSink
                )

                Pair(null, innerResult)
            } catch (e: Exception) {
                Pair(e, null)
            }
        }.flatMap { (ex, result) ->
            if (result != null) {
                Mono.just(result)
            } else {
                Mono.error(ex ?: IllegalStateException())
            }
        }
    }

    fun prepareAndApplyUpdate(
        resource: T,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
        propertyValueLocker: PropertyValueLocker,
    ): Mono<OperationSupplier> {
        return Mono.defer {
            // TODO: Lockers absent here, failing invariants
            val readTargetMono = selectReadResourceAsync(resource)
                            val writeTargetMono = selectWriteResourceAsync(resource)
                
            Mono.zip(
                readTargetMono,
                writeTargetMono,
            ).flatMap { (readTargetResource, writeTargetResource) ->
                logger.debug("$name:${resource.uuid} got r/w resources")

                val operationSupplierMono = prepareUpdateInnerAsync(
                    resource,
                    readTargetResource,
                    writeTargetResource,
                    volumeState,
                    artifactSink,
                    propertyValueLocker,
                )
                    .single()
                                        .doOnNext {
                        artifactSink.next(
                            ProcessorStatusArtifact(
                                name,
                                resource.uuid,
                                WorkflowProcessorStatus.STARTED
                            )
                        )
                        logger.debug("$name:${resource.uuid} got inner operation")
                    }
                    .doOnError { t ->
                        logger.debug("$name:${resource.uuid} failed to get inner operation: ${t::class.java.simpleName}")
                    }

                operationSupplierMono.map { innerOperationSupplier ->
                    val operationSupplier: OperationSupplier = {
                        var startTimeMs: Long? = null

                        logger.debug("$name:${resource.uuid} constructing aligner")
                        PartialTrackingResourceAligner(objectModelParser, writeTargetResource) { partialObjectGraph ->
                            logger.debug("$name:${resource.uuid} executing inner map")
                            innerOperationSupplier(partialObjectGraph)
                                .then()
                        }.apply()
                            .doOnSubscribe { startTimeMs = Instant.now().toEpochMilli() }
                            .thenReturn(Unit)
                            .doOnSuccess {
                                artifactSink.next(
                                    ProcessorStatusArtifact(
                                        name,
                                        resource.uuid,
                                        WorkflowProcessorStatus.SUCCEEDED
                                    )
                                )
                            }
                            .doOnError {
                                artifactSink.next(
                                    ProcessorStatusArtifact(
                                        name,
                                        resource.uuid,
                                        WorkflowProcessorStatus.FAILED
                                    )
                                )
                            }
                            .doOnNext {
                                logger.debug(
                                    "${this.name}:${resource.uuid} - ${
                                        Instant.now().toEpochMilli() - startTimeMs!!
                                    } ms"
                                )
                            }
                            .then()
                    }

                    operationSupplier
                }
            }
        }
            .onErrorResume { e ->
                when (e) {
                    is LockAcquireException -> Mono.error(
                        RequiredLockAcquireException(
                            e.kind,
                            e.resourceUuid,
                            e.resourceTypeDisplayName,
                            e.ownerOperationKey.toString(),
                            e.lockLevelRequested,
                            e.conflictingHolderKey.toString(),
                            e.conflictingHolderLockLevel,
                            e.conflictingHolderCreationTimestampMs,
                        )
                    )

                    is LockedResourceRenderException -> Mono.error(
                        // Resource locks were acquired but resource contains un-arbitrated values
                        RequiredLockedResourceRenderException(
                            e.resourceUuid,
                            e.resourceDisplayName,
                        )
                    )

                    else -> Mono.error(e)
                }
            }
    }

    fun checkPostConditions(
        resource: T,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val readTargetResource = findTargetResource(listOf(resource), targetResourceClass)
        val writeTargetResource = findTargetResource(listOf(resource), writeTargetResourceClass)

        checkPostConditions(
            resource,
            readTargetResource,
            writeTargetResource,
            volumeState,
            artifactSink
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowResourceUnaryFunction::class.java)
    }
}