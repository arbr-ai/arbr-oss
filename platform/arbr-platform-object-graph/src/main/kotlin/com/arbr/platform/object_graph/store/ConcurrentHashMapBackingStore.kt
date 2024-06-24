package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.ForeignKeyChildListResultStringImpl
import com.arbr.platform.object_graph.common.ForeignKeyChildSingleParentUpdate
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.util.OrderedMapCollector
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * innerKvStore: UUID -> OMR
 * foreignKeyIndexMap: FKey -> ParentUUID -> ChildUUID -> OMR
 * foreignKeyAcceptedIndexMap: FKey -> ParentUUID -> ChildUUID -> OMR
 * parallelInsertionOrderMap: UUID -> Ordinal
 */
class ConcurrentHashMapBackingStore<ForeignKey: NamedForeignKey>(
    private val innerKvStore: ConcurrentHashMap<String, ObjectModelResource<*, *, ForeignKey>>,
    private val foreignKeyIndexMap: ConcurrentHashMap<ForeignKey, ConcurrentHashMap<String, TreeMap<String, ObjectModelResource<*, *, ForeignKey>>>>,
    private val foreignKeyAcceptedIndexMap: ConcurrentHashMap<ForeignKey, ConcurrentHashMap<String, TreeMap<String, ObjectModelResource<*, *, ForeignKey>>>>,
    private val parallelInsertionOrderMap: ConcurrentHashMap<String, Int>,
) : KVStoreBackingObjectStore<ForeignKey, ObjectModelResource<*, *, ForeignKey>> {
    /**
     * Insertion index used for maintaining order among results
     */
    private val insertionIndex = AtomicInteger(0)

    override fun getObject(uuid: String): ObjectModelResource<*, *, ForeignKey>? = innerKvStore[uuid]

    override fun computeIfAbsent(
        key: String,
        mappingFunction: (String) -> ObjectModelResource<*, *, ForeignKey>,
        handleNewElement: (ObjectModelResource<*, *, ForeignKey>) -> Unit
    ): ObjectModelResource<*, *, ForeignKey> {
        var didCompute = false
        return innerKvStore.computeIfAbsent(key) { k ->
            val newValue = mappingFunction(k)
            didCompute = true
            newValue
        }.also {
            if (didCompute) {
                parallelInsertionOrderMap[key] = insertionIndex.getAndIncrement()
                handleNewElement(it)
            }
        }
    }

    override fun computeUpdatedForeignKeyChildren(
        childUpdate: ForeignKeyChildSingleParentUpdate<ForeignKey, ObjectModelResource<*, *, ForeignKey>>
    ): ForeignKeyChildListResultStringImpl<ObjectModelResource<*, *, ForeignKey>> {
        val foreignKey = childUpdate.foreignKey
        val parentUuid = childUpdate.parentUuid
        val isAccepted = childUpdate.isAccepted
        val childResource = childUpdate.child
        val updateDoesSetSameParent = childUpdate.updateDoesSetSameParent

        val outerMap = if (isAccepted) foreignKeyAcceptedIndexMap else foreignKeyIndexMap
        val foreignKeyMap = outerMap.computeIfAbsent(foreignKey) {
            ConcurrentHashMap()
        }
        val childList = foreignKeyMap.computeIfAbsent(parentUuid) {
            val children = queryForeignKey(foreignKey, parentUuid)

            TreeMap<String, ObjectModelResource<*, *, ForeignKey>>(compareBy {
                parallelInsertionOrderMap[it] ?: Int.MAX_VALUE
            })
                .also {
                    for (child in children) {
                        it[child.uuid] = child
                    }
                }
        }

        if (updateDoesSetSameParent) {
            childList.putIfAbsent(childResource.uuid, childResource)
        } else {
            childList.remove(childResource.uuid)
        }

        return ForeignKeyChildListResultStringImpl(
            parentUuid,
            isAccepted,
            childList.values.toList(),
        )
    }

    override fun queryForeignKey(foreignKey: ForeignKey, parentUuid: String): Collection<ObjectModelResource<*, *, ForeignKey>> {
        val collector = OrderedMapCollector<String, ObjectModelResource<*, *, ForeignKey>> { uuid ->
            getCreationOrdinal(uuid) ?: Int.MAX_VALUE
        }

        innerKvStore.filterTo(collector) { (_, obj) ->
            val fkPvs = obj.getForeignKeys()[foreignKey]
            fkPvs?.getLatestAcceptedValue()?.uuid == parentUuid || fkPvs?.getLatestValue()?.uuid == parentUuid
        }

        return collector
            .result()
    }

    override fun getCreationOrdinal(uuid: String): Int? = parallelInsertionOrderMap[uuid]
}