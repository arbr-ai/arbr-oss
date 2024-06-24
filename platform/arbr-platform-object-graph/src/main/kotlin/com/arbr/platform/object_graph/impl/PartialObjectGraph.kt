package com.arbr.platform.object_graph.impl

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.object_graph.alignable.PartialNodeAlignableValue
import com.arbr.platform.object_graph.alignable.PartialOperation


class PartialObjectGraph<T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>(
    private val rootUuid: String,
    private val nodeMap: MutableMap<String, Partial<*, *, ForeignKey>>,
) {
    val root: P by lazy {
        get<P>(rootUuid)!! // root must exist
    }

    fun <Q : Partial<*, Q, *>> get(
        uuid: String,
    ): Q? {
        @Suppress("UNCHECKED_CAST")
        return nodeMap[uuid] as? Q
    }

    fun toAlignableMap(): AlignableMap<PartialNodeAlignableValue, PartialOperation> {
        return AlignableMap(
            ImmutableLinkedMap(
                nodeMap.map { it.key to PartialNodeAlignableValue.fromPartialNode(it.value) }
            )
        )
    }

    fun didCreateNode(
        uuid: String,
        newPartial: Partial<*, *, ForeignKey>,
    ) {
        nodeMap[uuid] = newPartial
    }

    fun <
            U : ObjectModelResource<U, Q, ForeignKey>,
            Q : Partial<U, Q, ForeignKey>
            > didSetParent(
        partial: Q,
        key: ForeignKey,
        oldValue: String?,
        newValue: String?,
    ) {
        if (oldValue == newValue) {
            return
        }

        if (oldValue != null) {
            nodeMap[oldValue]?.let { oldParent ->
                oldParent.updateChildren(
                    key,
                    ChildCollectionUpdater.of<U, Q> { oldMap ->
                        oldMap?.let { om ->
                            ImmutableLinkedMap(
                                om.filter { (uuid, _) ->
                                    uuid != partial.uuid
                                }.toList()
                            )
                        }
                    }
                )
            }
        }

        if (newValue != null) {
            nodeMap[newValue]?.let { newParent ->
                newParent.updateChildren(
                    key,
                    ChildCollectionUpdater.of<U, Q> { newMap ->
                        newMap?.adding(partial.uuid, partial)
                            ?: ImmutableLinkedMap(partial.uuid to partial)
                    }
                )
            }
        }
    }

    fun didSetChildren(
        partial: Partial<*, *, *>,
        key: ForeignKey,
        oldValue: ImmutableLinkedMap<String, Partial<*, *, ForeignKey>>?,
        newValue: ImmutableLinkedMap<String, Partial<*, *, ForeignKey>>?,
    ) {
        val oldValueUuids = oldValue?.keys ?: emptySet()
        val newValueUuids = newValue?.keys ?: emptySet()

        val lostChildren = oldValueUuids - newValueUuids
        val gainedChildren = newValueUuids - oldValueUuids

        lostChildren.forEach { childUuid ->
            oldValue?.get(childUuid)?.updateParentConditionally(key, partial.uuid, null)
        }

        gainedChildren.forEach { childUuid ->
            newValue?.get(childUuid)?.updateParentConditionally(key, null, partial.uuid)
        }
    }

    private fun <U: ObjectModelResource<*, *, ForeignKey>, Q: Partial<*, *, ForeignKey>> populateNodeMap(
        resource: U,
    ) {
        if (resource.uuid in nodeMap) {
            return
        }

        val toPartial: Partial<*, *, ForeignKey> = resource.toPartialErased(this)
        nodeMap[resource.uuid] = toPartial

        resource.getChildren().forEach { (_, pvs) ->
            val latestAcceptedValue = pvs.getLatestAcceptedValue()
            latestAcceptedValue?.forEach { (_, ref) ->
                val objResource = ref.resourceErased()
                if (objResource != null) {
                    populateNodeMap(objResource)
                }
            }
        }
    }

    companion object {
        fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey> ofBaseResource(
            baseResource: T,
        ): PartialObjectGraph<T, P, ForeignKey> {
            return PartialObjectGraph<T, P, ForeignKey>(baseResource.uuid, mutableMapOf()).also {
                it.populateNodeMap(
                    baseResource
                )
            }
        }
    }

}
