package com.arbr.platform.data_structures_common.partial_order

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedSet


data class ConcretePartialOrderSet<V>(
    override val elements: Set<V>,
    val relationList: List<Pair<V, V>>,
    override val elementSoftOrder: Comparator<V>? = null
) : PartialOrderSet<V> {
    private val childMap by lazy {
        relationList
            .groupBy { it.first }
            .mapValues { (_, l) ->
                l.map { it.second }
            }
    }
    private val parentMap by lazy {
        relationList
            .groupBy { it.second }
            .mapValues { (_, l) ->
                l.map { it.first }
            }
    }

    override fun inferiorsOf(element: V): List<V> {
        return parentMap[element] ?: emptyList()
    }

    override fun superiorsOf(element: V): List<V> {
        return childMap[element] ?: emptyList()
    }

    override fun popAll(elementsToRemove: Collection<V>): ConcretePartialOrderSet<V> {
        val setToRemove = elementsToRemove.toSet()
        val newElements = elements.minus(setToRemove)
        val newRelationList = relationList.filter { it.first !in setToRemove }

        return ConcretePartialOrderSet(
            newElements,
            newRelationList,
        )
    }

    override fun pop(element: V): ConcretePartialOrderSet<V> {
        val newElements = elements.minus(element)
        val newRelationList = relationList.filter { it.first != element }

        return ConcretePartialOrderSet(
            newElements,
            newRelationList,
        )
    }

    /**
     * Get the minimal elements from the partial order.
     */
    override fun minima(): Collection<V> {
        val superiors = relationList.mapNotNull { (inf, sup) ->
            if (inf in elements) {
                sup
            } else {
                null
            }
        }.toSet()
        val minimalElements = elements.minus(superiors)

        val softOrder = elementSoftOrder
        return if (softOrder != null) {
            minimalElements.sortedWith(softOrder)
        } else {
            minimalElements
        }
    }

    /**
     * Get the maximal elements from the partial order.
     */
    override fun maxima(): Collection<V> {
        val inferiors = relationList.mapNotNull { (inf, sup) ->
            if (inf in elements && sup in elements) {
                inf
            } else {
                null
            }
        }.toSet()
        val maximalElements = elements.minus(inferiors)

        val softOrder = elementSoftOrder
        return if (softOrder != null) {
            maximalElements.sortedWith(softOrder)
        } else {
            maximalElements
        }
    }

    /**
     * Pop the minimal elements from the partial order and return the corresponding partial orders.
     */
    override fun popMinima(): List<Pair<V, ConcretePartialOrderSet<V>>> {
        return minima().map {
            it to pop(it)
        }
    }

    override fun pushAll(elements: Collection<V>): ConcretePartialOrderSet<V> {
        return ConcretePartialOrderSet(
            ImmutableLinkedSet(this.elements + elements),
            relationList,
        )
    }

    override fun pushAllRelationships(relations: List<Pair<V, V>>): ConcretePartialOrderSet<V> {
        return ConcretePartialOrderSet(
            immutableLinkedSetOf(elements),
            relationList + relations,
        )
    }

    override fun <W> map(f: (V) -> W): ConcretePartialOrderSet<W> {
        val nextElementMap = elements.associateWith(f)
        val nextRelationList = relationList.mapNotNull { (v0, v1) ->
            nextElementMap[v0]?.let { w0 ->
                nextElementMap[v1]?.let { w1 ->
                    w0 to w1
                }
            }
        }

        return ConcretePartialOrderSet(
            ImmutableLinkedSet(nextElementMap.values),
            nextRelationList,
        )
    }
}