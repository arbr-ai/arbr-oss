package com.arbr.platform.data_structures_common.partial_order

import java.util.*

object Orders {

    /**
     * Get all relation elements as pairs.
     * Ideally should move away from this representation as it's not super efficient.
     */
    fun <V> relationPairs(order: PartialOrder<V>): List<Pair<V, V>> {
        return order.elements.flatMap { v0 ->
            order.superiorsOf(v0)
                .map { v1 -> v0 to v1 }
        }
    }

    /**
     * Concatenate partial orders, with the each maximum of the first argument set to be a minimum of the latter.
     */
    fun <V> concatAll(firstPartialOrder: PartialOrder<V>, otherPartialOrders: List<PartialOrder<V>>): PartialOrder<V> {
        var resultPartialOrder: PartialOrder<V> = firstPartialOrder

        for (otherPartialOrder in otherPartialOrders) {
            val maxima = resultPartialOrder.maxima()
            val minima = otherPartialOrder.minima()

            for (max0 in maxima) {
                for (min1 in minima) {
                    resultPartialOrder = resultPartialOrder
                        .push(min1)
                        .push(max0)
                        .pushRelationship(max0, min1)
                }
            }

            // TODO: Make more efficient
            for (v in otherPartialOrder.elements) {
                resultPartialOrder = resultPartialOrder.push(v)
            }

            for ((v0, v1) in relationPairs(otherPartialOrder)) {
                resultPartialOrder = resultPartialOrder.pushRelationship(v0, v1)
            }
        }

        return resultPartialOrder
    }

    /**
     * Concatenate two partial orders, with the each maximum of the first argument set to be a minimum of the latter.
     */
    fun <V> concat(partialOrder: PartialOrder<V>, otherPartialOrder: PartialOrder<V>): PartialOrder<V> {
        return concatAll(partialOrder, listOf(otherPartialOrder))
    }

    fun <P : PartialOrder<V>, V : Any, Q : PartialOrder<W>, W : Any> mapView(
        outerPartialOrder: P,
        f: (V) -> Q,
    ): MapViewPartialOrder<V, W> {
        val pairElements = mutableSetOf<Pair<Optional<V>, W>>()
        val innerRelations = mutableListOf<Pair<Pair<Optional<V>, W>, Pair<Optional<V>, W>>>()
        outerPartialOrder.map { v ->
            f(v).map { w ->
                Optional.of(v) to w
            }
        }
            .bfs {
                pairElements.addAll(it.elements)
                innerRelations.addAll(relationPairs(it))
            }

        return MapViewPartialOrder(pairElements, innerRelations)
    }

    fun <V> unionAll(
        firstPartialOrder: PartialOrder<V>,
        otherPartialOrders: List<PartialOrder<V>>
    ): PartialOrder<V> {
        return otherPartialOrders.fold(firstPartialOrder) { acc, e ->
            acc.pushAll(e.elements)
                .pushAllRelationships(relationPairs(e))
        }
    }

    fun <V> union(
        partialOrder0: PartialOrder<V>,
        partialOrder1: PartialOrder<V>,
    ): PartialOrder<V> {
        return unionAll(partialOrder0, listOf(partialOrder1))
    }

    /**
     * Type-preserving binary operators
     */

    private fun <P : PartialOrder<V>, V> softCastInto(
        order: P,
        untyped: PartialOrder<V>,
    ): P {
        @Suppress("UNCHECKED_CAST")
        return when (order as PartialOrder<V>) {
            is LinearOrderList -> if (untyped is LinearOrderList) {
                untyped
            } else {
                untyped.asLinearOrderList()
            }

            is MapViewPartialOrder<*, *> -> if (untyped is MapViewPartialOrder<*, *>) {
                untyped
            } else {
                throw Exception("Expected MapViewPartialOrder from type-preserving operation but got ${untyped::class.java.name}")
            }

            is PartialOrderSet -> if (untyped is PartialOrderSet<*>) {
                untyped
            } else {
                untyped.asPartialOrderSet()
            }
        } as P
    }

    fun <P : PartialOrder<V>, V> empty(
        order: P,
    ): P {
        @Suppress("UNCHECKED_CAST")
        return when (order as PartialOrder<V>) {
            is LinearOrderList -> LinearOrderList(emptyList())
            is MapViewPartialOrder<*, *> -> order.popAll(order.elements)
            is PartialOrderSet -> emptyPoset()
        } as P
    }

    fun <P : PartialOrder<V>, V> filter(
        order: P,
        f: (V) -> Boolean,
    ): P {
        val relationList = relationPairs(order)

        val toRemove = order.elements.filterNot(f)
            .associateWith {
                mutableListOf<V>() to mutableListOf<V>() // Downward to Upward
            }
            .toMutableMap()
        val indicesToRemove = mutableSetOf<Int>()

        for ((i, p) in relationList.withIndex()) {
            val (v0, v1) = p
            if (v0 in toRemove) {
                val (_, upwardList) = toRemove[v0]!!
                upwardList.add(v1)
                indicesToRemove.add(i)
            }
            if (v1 in toRemove) {
                val (downwardList, _) = toRemove[v1]!!
                downwardList.add(v0)
                indicesToRemove.add(i)
            }
        }

        val newRelationships = mutableListOf<Pair<V, V>>()
        for ((downwardList, upwardList) in toRemove.values) {
            for (v0 in downwardList) {
                for (v1 in upwardList) {
                    newRelationships.add(v0 to v1)
                }
            }
        }

        return softCastInto(
            order,
            order.popAll(
                toRemove.keys,
            ).pushAllRelationships(newRelationships)
        )
    }

    fun <V, W, Q : PartialOrder<W>> mapInto(
        order: Q,
        fromPartialOrder: PartialOrder<V>,
        f: (V) -> W,
    ): Q {
        val nextElementMap = fromPartialOrder.elements.associateWith(f)

        // Should we remove self-edges by default? Yes
        val nextRelationList = relationPairs(fromPartialOrder).mapNotNull { (v0, v1) ->
            nextElementMap[v0]?.let { w0 ->
                nextElementMap[v1]?.let { w1 ->
                    if (w0 == w1) {
                        null
                    } else {
                        w0 to w1
                    }
                }
            }
        }

        return softCastInto(
            order,
            order.pushAll(
                nextElementMap.values,
            ).pushAllRelationships(nextRelationList)
        )
    }

    fun <V, W, Q : PartialOrder<W>> mapIntoNotNull(
        order: Q,
        fromPartialOrder: PartialOrder<V>,
        f: (V) -> W?,
    ): Q {
        val nullableTargetPorder = mapInto(
            emptyPoset(),
            fromPartialOrder,
            f
        )
        val filtered = filter(nullableTargetPorder) { it != null }

        return mapInto(
            order,
            filtered,
        ) { it!! }
    }

    data class IndexKeyedValue<V>(
        override val key: Int,
        val element: V,
    ) : KeyedValue<Int>

    fun <V> keyedListPoset(
        elementList: List<V>,
    ): KeyedPartialOrderSet<Int, IndexKeyedValue<V>> {
        val keyedElements = elementList.mapIndexed { index, v ->
            IndexKeyedValue(index, v)
        }
            .associateBy { it.key }

        val nodes = elementList.indices.associateWith { i ->
            val infs = if (i == 0) {
                emptySet()
            } else {
                setOf(i - 1)
            }
            val sups = if (i == elementList.size - 1) {
                emptySet()
            } else {
                setOf(i + 1)
            }

            KeyedPartialOrderSet.KeyNode(
                i,
                infs,
                sups,
            )
        }

        return KeyedPartialOrderSet(
            keyedElements,
            MaskedMap(nodes, mutableSetOf()) { elt, masks ->
                elt.copy(
                    inferiors = elt.inferiors - masks,
                    superiors = elt.superiors - masks,
                )
            },
        )
    }

}