package com.arbr.platform.object_graph.alignable

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.alignable.alignable.AtomicAlignable
import com.arbr.platform.alignable.alignable.IdentifiableAlignable
import com.arbr.platform.alignable.alignable.SwapAlignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.alignable.alignable.collections.AlignableKeyValue
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.platform.data_structures_common.partial_order.LinearOrderList
import com.arbr.platform.object_graph.common.ObjectValueEquatable
import com.arbr.platform.object_graph.impl.PartialNode

typealias ForeignAlignmentKey = Int

data class PartialNodeAlignableValue(
    override val uuid: AtomicAlignable<String>,
    val typeName: AtomicAlignable<String>,
    /**
     * Parent foreign keys
     * absent = undecided
     * present & null = known no parent
     */
    val parentUuids: AlignableKeyValue<ForeignAlignmentKey, PartialRefAlignable, String?>,
    /**
     * Child container UUIDs
     * present iff nonempty
     */
    val childContainerUuids: AlignableKeyValue<ForeignAlignmentKey, SwapAlignable<ForeignAlignmentKey>, ForeignAlignmentKey>,
    val properties: AlignableMap<SwapAlignable<ObjectValueEquatable<out Any?>>, ObjectValueEquatable<out Any?>>,
) : IdentifiableAlignable<PartialNodeAlignableValue, PartialOperation> {
    override fun align(e: PartialNodeAlignableValue): Alignment<PartialNodeAlignableValue, PartialOperation> {
        val a1 = uuid.align(e.uuid)
        val a1Ops: List<PartialOperation> = a1.operations.map {
            PartialOperation(it, null, null, null, null)
        }

        val a2 = typeName.align(e.typeName)
        val a2Ops: List<PartialOperation> = a2.operations.map {
            PartialOperation(null, it, null, null, null)
        }

        val a3 = parentUuids.align(e.parentUuids)
        val a3Ops: List<PartialOperation> = a3.operations.map {
            PartialOperation(null, null, it, null, null)
        }

        val a4 = childContainerUuids.align(e.childContainerUuids)
        val a4Ops: List<PartialOperation> = a4.operations.map {
            PartialOperation(null, null, null, it, null)
        }

        val a5 = properties.align(e.properties)
        val a5Ops: List<PartialOperation> = a5.operations.map {
            PartialOperation(null, null, null, null, it)
        }

        return if (a1 is Alignment.Equal && a2 is Alignment.Equal && a3 is Alignment.Equal && a4 is Alignment.Equal && a5 is Alignment.Equal) {
            Alignment.Equal(this, e)
        } else {
            Alignment.Align(
                a1Ops + a2Ops + a3Ops + a4Ops + a5Ops,
                (a1.cost) + (a2.cost) + (a3.cost) + (a4.cost) + (a5.cost),
                this,
                e,
            )
        }
    }

    override fun empty(): PartialNodeAlignableValue {
        // Preserve "constant" values
        // Are there cases where we shouldn't delete / re-add property and parent values? Like:
        // ObjectModel.kvStore[uuid.element]?.nodeValueAccepted() ?: ...
        return PartialNodeAlignableValue(
            uuid,
            typeName,
            parentUuids.empty(),
            childContainerUuids,
            properties.empty(),
        )
    }

    override fun applyAlignment(alignmentOperations: List<PartialOperation>): PartialNodeAlignableValue {
        val e1 = uuid.applyAlignment(alignmentOperations.mapNotNull { it.uuid })
        val e2 = typeName.applyAlignment(alignmentOperations.mapNotNull { it.typeName })
        val e3 = parentUuids.applyAlignment(LinearOrderList(alignmentOperations.mapNotNull { it.parentUuids }))
        val e4 =
            AlignableKeyValue(
                childContainerUuids.applyAlignment(LinearOrderList(alignmentOperations.mapNotNull { it.childContainerUuids })).elements
            )
        val e5 = properties.applyAlignment(LinearOrderList(alignmentOperations.mapNotNull { it.properties }))
        return PartialNodeAlignableValue(e1, e2, e3, e4, e5)
    }

    fun getParent(foreignKey: NamedForeignKey): PartialRefAlignable? {
        return parentUuids[foreignKey.ordinal]
    }

    companion object {
        fun fromPartialNode(partialNode: PartialNode<*, *>): PartialNodeAlignableValue {
            val uuid = AtomicAlignable(partialNode.uuid)
            val typeName = AtomicAlignable(partialNode.resourceTypeName)
            val parentUuids = AlignableKeyValue(
                ImmutableLinkedMap<ForeignAlignmentKey, PartialRefAlignable>(
                    partialNode.parents.parentMap
                        .map { it.key.ordinal to PartialRefAlignable(it.value.uuid) }
                ),
            )
            val elements = partialNode
                .children
                .foreignKeyChildMap
                .map { (foreignKey, _) ->
                    foreignKey.ordinal to SwapAlignable(foreignKey.ordinal)
                }
            val childContainerUuids = AlignableKeyValue(
                ImmutableLinkedMap<ForeignAlignmentKey, SwapAlignable<ForeignAlignmentKey>>(
                    elements
                ),
            )
            val properties = AlignableMap(
                ImmutableLinkedMap(
                    partialNode.properties.propertyMap
                        .entries
                        .mapNotNull {
                            it.value?.let { v ->
                                @Suppress("DEPRECATION")
                                it.key to SwapAlignable(v.simpleEquatableValue())
                            }
                        }
                )
            )
            return PartialNodeAlignableValue(
                uuid,
                typeName,
                parentUuids,
                childContainerUuids,
                properties,
            )
        }

    }
}
