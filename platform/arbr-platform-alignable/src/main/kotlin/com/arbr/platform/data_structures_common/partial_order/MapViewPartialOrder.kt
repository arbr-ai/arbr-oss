package com.arbr.platform.data_structures_common.partial_order

import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * PartialOrder that provides a view into a mapped poset. Specifically, it can behave like an order in the target type
 * while preserving source values in pairs, with Optional.empty filling in for new values with no inverses.
 */
class MapViewPartialOrder<
        V : Any,
        U,
        >(
    private val pairedElements: Set<Pair<Optional<V>, U>>,
    private val pairedRelationList: List<Pair<Pair<Optional<V>, U>, Pair<Optional<V>, U>>>,
) : PartialOrderSet<U> {

    override val elements: Set<U> = pairedElements.map { it.second }.toSet()
    private val relationList: List<Pair<U, U>> = pairedRelationList.map { it.first.second to it.second.second }

    private val reverseLookup = pairedElements.associate { it.second to it.first.getOrNull() }

    private val childMap = relationList
        .groupBy { it.first }
        .mapValues { (_, l) ->
            l.map { it.second }
        }
    private val parentMap = relationList
        .groupBy { it.second }
        .mapValues { (_, l) ->
            l.map { it.first }
        }

    fun pairedView(): PartialOrderSet<Pair<Optional<V>, U>> = ConcretePartialOrderSet(
        pairedElements,
        pairedRelationList,
    )

    override fun inferiorsOf(element: U): List<U> {
        return parentMap[element] ?: emptyList()
    }

    override fun superiorsOf(element: U): List<U> {
        return childMap[element] ?: emptyList()
    }

    /**
     * Get the minimal elements from the partial order.
     */
    override fun minima(): List<U> {
        val superiors = relationList.mapNotNull { (inf, sup) ->
            if (inf in elements) {
                sup
            } else {
                null
            }
        }.toSet()
        val minimalElements = elements.filter { it !in superiors }

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
    override fun maxima(): List<U> {
        val inferiors = relationList.mapNotNull { (inf, sup) ->
            if (inf in elements && sup in elements) {
                inf
            } else {
                null
            }
        }.toSet()
        val maximalElements = elements.filter { it !in inferiors }

        val softOrder = elementSoftOrder
        return if (softOrder != null) {
            maximalElements.sortedWith(softOrder)
        } else {
            maximalElements
        }
    }

    override fun popMinima(): List<Pair<U, MapViewPartialOrder<V, U>>> {
        return minima().map { elt ->
            elt to pop(elt)
        }
    }

    override fun <W> map(f: (U) -> W): MapViewPartialOrder<V, W> {
        val nextElementMapV = pairedElements.associate {
            it.first to f(it.second)
        }
        val nextRelationListV: List<Pair<Pair<Optional<V>, W>, Pair<Optional<V>, W>>> = pairedRelationList.mapNotNull { (v0, v1) ->
            nextElementMapV[v0.first]?.let { w0 ->
                nextElementMapV[v1.first]?.let { w1 ->
                    (v0.first to w0) to (v1.first to w1)
                }
            }
        }
        val nextPairedElements: Set<Pair<Optional<V>, W>> = nextElementMapV.toList().toSet()

        return MapViewPartialOrder<V, W>(
            nextPairedElements,
            nextRelationListV,
        )
    }

    override fun pushAllRelationships(relations: List<Pair<U, U>>): MapViewPartialOrder<V, U> {
        val pairedRelations = relations.map { (inferior, superior) ->
            // Look for existing
            val existingInfSource = reverseLookup[inferior]
            val existingSupSource = reverseLookup[superior]

            (Optional.ofNullable(existingInfSource) to inferior) to (Optional.ofNullable(existingSupSource) to superior)
        }

        return MapViewPartialOrder(
            pairedElements,
            pairedRelationList + pairedRelations
        )
    }

    override fun pushRelationship(inferior: U, superior: U): MapViewPartialOrder<V, U> = pushAllRelationships(listOf(inferior to superior))

    override fun pushAll(elements: Collection<U>): MapViewPartialOrder<V, U> {
        val emptyPairedNewElements = elements.map { Optional.empty<V>() to it }
        return MapViewPartialOrder(
            pairedElements + emptyPairedNewElements,
            pairedRelationList
        )
    }

    override fun push(element: U): MapViewPartialOrder<V, U> = pushAll(listOf(element))

    override fun pop(element: U): MapViewPartialOrder<V, U> {
        return MapViewPartialOrder(
            pairedElements.filter { it.second != element }.toSet(),
            pairedRelationList.filter { (p0, p1) ->
                p0.second != element && p1.second != element
            }
        )
    }

    override fun popAll(elementsToRemove: Collection<U>): MapViewPartialOrder<V, U> {
        return MapViewPartialOrder(
            pairedElements.filter { it.second !in elementsToRemove }.toSet(),
            pairedRelationList.filter { (p0, p1) ->
                p0.second !in elementsToRemove && p1.second !in elementsToRemove
            }
        )
    }

    override fun dfsPreAndPostfix(pre: (U?) -> Unit, post: (U?) -> Unit) {
        val added = mutableSetOf<U>()

        val stack = Stack<Pair<U?, (() -> Unit)?>>()
        stack.push(null to { post(null) })  // Null value signals root
        pre(null)
        val rootElements = minima()
            .reversed() // reverse in case there's a soft order
        added.addAll(rootElements)
        stack.addAll(
            rootElements
                .map { it to null }
        )

        var runningPoset = this
        while (stack.isNotEmpty()) {
            val (minimum, callback) = stack.pop()

            if (callback != null) {
                callback()
            }

            if (minimum != null) {
                stack.push(null to { post(minimum) })
                runningPoset = runningPoset.pop(minimum)

                pre(minimum)
                val childElements = runningPoset
                    .minima()
                    .reversed() // reverse in case there's a soft order
                stack.addAll(
                    childElements
                        .filter { it !in added }
                        .map { it to null }
                )
                added.addAll(childElements)
            }
        }
    }

    override fun bfs(f: (U) -> Unit) {
        val seen = mutableSetOf<U>()

        var posetFrontier = listOf(this)

        while (posetFrontier.isNotEmpty()) {
            val minimaAndSubPosets = posetFrontier.flatMap { it.popMinima() }

            val nextPosetFrontier = mutableListOf<MapViewPartialOrder<V, U>>()
            for ((minimum, subPoset) in minimaAndSubPosets) {
                if (minimum !in seen) {
                    f(minimum)
                    seen.add(minimum)
                    nextPosetFrontier.add(subPoset)
                }
            }

            posetFrontier = nextPosetFrontier
        }
    }
}