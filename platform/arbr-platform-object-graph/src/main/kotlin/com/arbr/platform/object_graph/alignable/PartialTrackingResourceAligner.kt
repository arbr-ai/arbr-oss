package com.arbr.platform.object_graph.alignable

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.SwapAlignable
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.alignable.alignable.collections.KeyValueAlignmentOperation
import com.arbr.platform.alignable.alignable.collections.MapAlignmentListener
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation
import com.arbr.platform.object_graph.common.ObjectValueEquatable
import com.arbr.platform.object_graph.core.ObjectModelParser
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * TODO: Refactor - this class is becoming monolithic and hard to test
 */
data class PartialTrackingResourceAligner<T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey>(
    private val objectModelParser: ObjectModelParser,
    private val baseResource: T,
    private val innerMap: (PartialObjectGraph<T, P, ForeignKey>) -> Mono<Void>
) : MapAlignmentListener<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>() {

    override val uuid: String
        get() = UUID.randomUUID().toString()

    private val nodeMap = mutableMapOf<String, ObjectModelResource<*, *, ForeignKey>>()

    private val objectGraph = PartialObjectGraph.ofBaseResource(baseResource)

    private val baseAlignableNodeMap = objectGraph.toAlignableMap()

    private data class ResourceUpdate(
        val description: String,
        val operation: Mono<Void>,
    )

    private val updates = mutableListOf<ResourceUpdate>()

    init {
        populateNodeMap(baseResource, nodeMap)
    }

    /**
     * Populate node map extending from root resource given
     */
    private fun populateNodeMap(
        fromNode: ObjectModelResource<*, *, ForeignKey>,
        newNodeMap: MutableMap<String, ObjectModelResource<*, *, ForeignKey>>
    ) {
        if (fromNode.uuid in newNodeMap) {
            return
        }
        newNodeMap[fromNode.uuid] = fromNode

        fromNode.getChildren().mapValues { (_, chl) ->
            chl.getLatestAcceptedValue()?.forEach { (_, nodeRef) ->
                val resource = nodeRef.resourceErased()
                if (resource != null) {
                    populateNodeMap(resource, newNodeMap)
                }
            }
        }
    }

    private fun applyInnerMap(): Mono<AlignableMap<PartialNodeAlignableValue, PartialOperation>> {
        return innerMap(objectGraph).then(Mono.fromCallable {
            logger.debug("[${uuid.takeLast(4)}] Converting to alignable map")
            objectGraph.toAlignableMap()
                .also {
                    logger.debug("[${uuid.takeLast(4)}] Converted to alignable map")
                }
        })
    }

    private fun initializeSourceValue(
        nodeValue: PartialNodeAlignableValue,
    ) {
        // Shallow initialization - node only
        nodeMap.computeIfAbsent(nodeValue.uuid.element) {
            logger.debug("Node init ${nodeValue.uuid.element}")
            objectModelParser.parseNodeValue(nodeValue)
        }
    }

    override fun didInsertElement(
        operationIndex: Int,
        key: String,
        element: AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>
    ) {
        val nodeValue = element.alignableElement
        logger.debug("Element insert ${nodeValue.uuid.element}")
        if (element.sourceValue == null) {
            initializeSourceValue(nodeValue)
        }
    }

    override fun didRemoveElement(
        operationIndex: Int,
        key: String,
        element: AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>
    ) {
        val uuid = element.alignableElement.uuid.element
        logger.debug("Element remove ${uuid}")
        nodeMap.remove(uuid)
    }

    override fun didEditElement(
        operationIndex: Int,
        key: String,
        alignment: List<PartialOperation>,
        fromElement: AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>,
        mapElement: AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>
    ) {
        logger.debug("Element edit ${mapElement.alignableElement.uuid.element}")
        val sourceValue = mapElement.sourceValue ?: nodeMap[mapElement.alignableElement.uuid.element]!!
        for (op in alignment) {
            updates.addAll(applySourceNodeOperation(sourceValue, op))
        }
    }

    override fun didEnterMap(map: AlignableMap<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>) {
        //
    }

    override fun didExitMap(map: AlignableMap<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>) {
        //
    }

    private fun parentIdOperation(
        parentIdsOperation: KeyValueAlignmentOperation<ForeignAlignmentKey, PartialRefAlignable, String?>,
        sourceNodeValue: ObjectModelResource<*, *, ForeignKey>
    ): ResourceUpdate? {
        val key = parentIdsOperation.atKey
        val oldElement = when (parentIdsOperation) {
            is KeyValueAlignmentOperation.Delete -> parentIdsOperation.element
            is KeyValueAlignmentOperation.Edit -> parentIdsOperation.fromElement
            is KeyValueAlignmentOperation.Insert -> null
        }
        val newElement = when (parentIdsOperation) {
            is KeyValueAlignmentOperation.Delete -> null
            is KeyValueAlignmentOperation.Edit -> parentIdsOperation.element
            is KeyValueAlignmentOperation.Insert -> parentIdsOperation.element
        }

        if (oldElement == newElement) {
            // No update
            return null
        }

        val newUuid = newElement?.uuid

        val propertyDescriptor = "${sourceNodeValue.resourceTypeName}[${sourceNodeValue.uuid}].Parent[$key]"
        val opDescription = when (parentIdsOperation) {
            is KeyValueAlignmentOperation.Delete -> "del $propertyDescriptor -= ${parentIdsOperation.element.uuid}"
            is KeyValueAlignmentOperation.Edit -> {
                val fromValue = parentIdsOperation.fromElement.uuid
                val value = parentIdsOperation.element.uuid
                "set $propertyDescriptor : $fromValue -> $value"
            }

            is KeyValueAlignmentOperation.Insert -> {
                val value = parentIdsOperation.element.uuid

                "ins $propertyDescriptor += $value"
            }
        }

        val childUpdate = if (newElement != null) {
            val foreignKey = sourceNodeValue.getForeignKeys().keys.firstOrNull { it.ordinal == key }
                ?: throw Exception("No key")

            ResourceUpdate(
                opDescription,
                sourceNodeValue.setParent(foreignKey, newUuid),
            )
        } else {
            null
        }

        return childUpdate
    }

    private fun childContainerOperation(
        childContainerOperation: KeyValueAlignmentOperation<ForeignAlignmentKey, SwapAlignable<ForeignAlignmentKey>, ForeignAlignmentKey>,
        sourceNodeValue: ObjectModelResource<*, *, ForeignKey>,
    ): ResourceUpdate? {

        return when (childContainerOperation) {
            is KeyValueAlignmentOperation.Insert -> {
                val insertKey = childContainerOperation.element.element
                val propertyDescriptor =
                    "${sourceNodeValue.resourceTypeName}[${sourceNodeValue.uuid}].Container[$insertKey]"
                // TODO: Do this better
                val foreignKey = sourceNodeValue.getChildren().keys.firstOrNull { it.ordinal == insertKey }
                    ?: throw Exception("No key")
                val updateMono = sourceNodeValue.getChildren()[foreignKey]!!.proposeWith {
                    Mono.just(it ?: ImmutableLinkedMap())
                }.then()

                ResourceUpdate("ins $propertyDescriptor", updateMono)
            }

            is KeyValueAlignmentOperation.Delete -> null
            is KeyValueAlignmentOperation.Edit -> null
        }
    }

    private fun propertyMapOperation(
        mapOperation: MapAlignmentOperation<SwapAlignable<ObjectValueEquatable<*>>, ObjectValueEquatable<*>>,
        sourceNodeValue: ObjectModelResource<*, *, ForeignKey>
    ): ResourceUpdate? {
        val key = mapOperation.atKey
        val objectValue = mapOperation.element.element.objectValue

        val properties = sourceNodeValue.properties()
        val property = properties[key]
            ?: run {
                logger.error("Property $key not found on ${sourceNodeValue.resourceTypeName} for property map operation")
                return null
            }

        // TODO: Support deletes more directly
        val updateMono = property.proposeAsync {
            Mono.justOrEmpty(
                when (mapOperation) {
                    is MapAlignmentOperation.Delete -> objectValue
                    is MapAlignmentOperation.Edit -> objectValue
                    is MapAlignmentOperation.Insert -> objectValue
                }
            )
        }.then()

        val propertyDescriptor =
            "${property.identifier.resourceKey.name}[${property.identifier.resourceUuid}].${property.identifier.propertyKey.name}"
        val valueLogLengthLimit = 32
        val opDescription = when (mapOperation) {
            is MapAlignmentOperation.Delete -> "del $propertyDescriptor"
            is MapAlignmentOperation.Edit -> {
                val fromValue = mapOperation.fromElement.element.objectValue.value.toString()
                    .let {
                        if (it.length > valueLogLengthLimit) {
                            it.take(valueLogLengthLimit) + "..."
                        } else {
                            it
                        }
                    }
                val value = mapOperation.element.element.objectValue.value.toString()
                    .let {
                        if (it.length > valueLogLengthLimit) {
                            it.take(valueLogLengthLimit) + "..."
                        } else {
                            it
                        }
                    }
                "set $propertyDescriptor : $fromValue -> $value"
            }

            is MapAlignmentOperation.Insert -> {
                val value = mapOperation.element.element.objectValue.value.toString()
                    .let {
                        if (it.length > valueLogLengthLimit) {
                            it.take(valueLogLengthLimit) + "..."
                        } else {
                            it
                        }
                    }

                "ins $propertyDescriptor = $value"
            }
        }

        return ResourceUpdate(
            opDescription,
            updateMono
        )
    }

    private fun applySourceNodeOperation(
        sourceNodeValue: ObjectModelResource<*, *, ForeignKey>,
        operation: PartialOperation
    ): List<ResourceUpdate> {
        val parentIdsOperation = if (operation.parentUuids == null) {
            null
        } else {
            parentIdOperation(operation.parentUuids, sourceNodeValue)
        }
        val childContainerOperation = if (operation.childContainerUuids == null) {
            null
        } else {
            childContainerOperation(operation.childContainerUuids, sourceNodeValue)
        }
        val mapOperation = if (operation.properties == null) {
            null
        } else {
            propertyMapOperation(operation.properties, sourceNodeValue)
        }
        return listOfNotNull(
            parentIdsOperation,
            childContainerOperation,
            mapOperation,
        )
    }

    fun apply(): Mono<Void> {
        val logId = uuid.takeLast(4)

        val targetMapMono: Mono<AlignableMap<PartialNodeAlignableValue, PartialOperation>> = applyInnerMap()

        return targetMapMono.flatMap { targetMap: AlignableMap<PartialNodeAlignableValue, PartialOperation> ->

            logger.debug("[${logId}] Got target alignable map with ${targetMap.size} nodes")

            val elements = baseAlignableNodeMap
                .mapNotNull { nodeMap[it.key]?.let { omr -> omr to it.value } }
                .map { (omr, node) ->
                    omr.uuid to AlignableProxy(
                        omr,
                        node,
                    )
                }
            val partialNodeMap =
                ImmutableLinkedMap(
                    elements
                )

            val alignablePairedMap = PartialTrackingAlignableMap(partialNodeMap)

            val pairedTargetMap: AlignableMap<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation> =
                AlignableMap(
                    ImmutableLinkedMap(
                        targetMap.map { (k, node) ->
                            k to AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>(
                                null,
                                node
                            )
                        }.toList()
                    )
                )

            alignablePairedMap.addListener(this)
            pairedTargetMap.addListener(this)

            logger.debug("[${logId}] Will align maps")
            val pairedAlignment = alignablePairedMap.align(pairedTargetMap)

            logger.debug("[${logId}] Will apply alignment")
            alignablePairedMap
                .applyAlignment(pairedAlignment.operations)
            logger.debug("[${logId}] Applied alignment")

            Flux.fromIterable(updates).flatMap { it.operation.thenReturn(Unit) }.collectList()
                .doOnNext {
                    logger.info("[$logId] Completed ${it.size} updates")

//                    for (update in updates) {
//                        updateLogger.info("[$logId] ${update.description}")
//                    }
                }
                .then()
        }.then()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PartialTrackingResourceAligner::class.java)
        private val updateLogger =
            LoggerFactory.getLogger("com.arbr.og.alignable.PartialTrackingResourceAlignerUpdateLogger")
    }
}
