package com.arbr.platform.data_structures_common.partial_order

import java.util.*

interface KeyedValue<K : Comparable<K>> {

    val key: K
}

class MaskedMap<K, V>(
    private val innerMap: Map<K, V>,
    private val maskedKeys: MutableSet<K>,
    private val recompute: (V, MutableSet<K>) -> V,
) {

    private var innerKeys = innerMap.keys - maskedKeys

    val keys: Set<K> get() = innerKeys

    fun mask(k: K) {
        maskedKeys.add(k)
    }

    operator fun get(k: K): V? {
        return if (k in maskedKeys) {
            null
        } else {
            innerMap[k]?.let { recompute(it, maskedKeys) }
        }
    }

    fun filterValues(f: (V) -> Boolean): Map<K, V> {
        return innerMap
            .filterKeys {  k ->
                k !in maskedKeys && get(k)?.let(f) == true
            }
    }

    fun mapValues(f: (Pair<K, V>) -> V): MaskedMap<K, V> {
        val nextMap = mutableMapOf<K, V>()
        for (e in innerMap.entries) {
            val (k, _) = e
            if (k in maskedKeys) {
                continue
            }

            val v = get(k) ?: continue
            nextMap[k] = f(k to v)
        }

        return MaskedMap(
            nextMap,
            maskedKeys,
            recompute
        )
    }

    fun copy(): MaskedMap<K, V> {
        return MaskedMap(innerMap, maskedKeys.toMutableSet(), recompute)
    }
}

data class KeyedPartialOrderSet<K : Comparable<K>, V : KeyedValue<K>>(
    /**
     * List of elements. Note for usage: this effectively represents the universe of values and not strictly the ones
     * that are members of this poset.
     */
    private val elementMap: Map<K, V>,
    private val nodeMap: MaskedMap<K, KeyNode<K>>,
) : PartialOrderSet<V> {

    data class KeyNode<K : Comparable<K>>(
        val key: K,
        val inferiors: Set<K>,
        val superiors: Set<K>,
    )

    override val elementSoftOrder: Comparator<V> = compareBy { it.key }

    private val minimalKeys: List<K> by lazy {
        nodeMap.filterValues { it.inferiors.isEmpty() }.keys.sorted()
    }
    private val maximalKeys: List<K> by lazy {
        nodeMap.filterValues { it.superiors.isEmpty() }.keys.sorted()
    }

    override val elements: List<V> by lazy {
        nodeMap.keys.mapNotNull { elementMap[it] }
    }

    override fun superiorsOf(element: V): List<V> {
        val childIndices = nodeMap[element.key]?.superiors ?: emptyList()
        return childIndices.mapNotNull { elementMap[it] }
    }

    override fun inferiorsOf(element: V): List<V> {
        val parentIndices = nodeMap[element.key]?.inferiors ?: emptyList()
        return parentIndices.mapNotNull { elementMap[it] }
    }

    override fun popAll(elementsToRemove: Collection<V>): KeyedPartialOrderSet<K, V> {
        val indicesToRemove = elementsToRemove.map { it.key }.toSet()

        val nextNodeMap = nodeMap.copy()
        for (idx in indicesToRemove) {
            nextNodeMap.mask(idx)
        }

        // Importantly we don't filter the elements here because we need to preserve the indices
        // This has the added value of not requiring constantly reconstructing lists
        return KeyedPartialOrderSet(
            elementMap,
            nextNodeMap,
        )
    }

    override fun pop(element: V): KeyedPartialOrderSet<K, V> {
        return popAll(listOf(element))
    }

    /**
     * Get the minimal elements from the partial order.
     */
    override fun minima(): List<V> {
        return minimalKeys.mapNotNull { elementMap[it] }
    }

    /**
     * Get the maximal elements from the partial order.
     */
    override fun maxima(): List<V> {
        return maximalKeys.mapNotNull { elementMap[it] }
    }

    /**
     * Pop the minimal elements from the partial order and return the corresponding partial orders.
     */
    override fun popMinima(): List<Pair<V, KeyedPartialOrderSet<K, V>>> {
        return minima().map {
            it to pop(it)
        }
    }

    override fun pushAll(elements: Collection<V>): KeyedPartialOrderSet<K, V> {
        // Need to assume elements will take indices in the appended list regardless of what they were before
        return KeyedPartialOrderSet(
            elementMap.plus(
                elements.associateBy { it.key }
            ),
            nodeMap
        )
    }

    override fun pushAllRelationships(relations: List<Pair<V, V>>): KeyedPartialOrderSet<K, V> {

        val updateMap = mutableMapOf<K, Pair<MutableSet<K>, MutableSet<K>>>()

        for ((inf, sup) in relations) {
            if (inf.key !in updateMap) {
                updateMap[inf.key] = nodeMap[inf.key]?.let {
                    it.inferiors.toMutableSet() to it.superiors.toMutableSet()
                } ?: (mutableSetOf<K>() to mutableSetOf())
            }
            updateMap[inf.key]!!.second.add(sup.key)

            if (sup.key !in updateMap) {
                updateMap[sup.key] = nodeMap[sup.key]?.let {
                    it.inferiors.toMutableSet() to it.superiors.toMutableSet()
                } ?: (mutableSetOf<K>() to mutableSetOf())
            }
            updateMap[sup.key]!!.first.add(inf.key)
        }

        val nextNodeMap = nodeMap
            .mapValues { (k, n) ->
                updateMap[k]?.let {
                    KeyNode(k, it.first, it.second)
                } ?: n
            }

        return KeyedPartialOrderSet(
            elementMap,
            nextNodeMap,
        )
    }

    override fun <W> map(f: (V) -> W): PartialOrder<W> {
        throw Exception("Do not map KPoset")
//        val nextElementMap = elements
//            .associate { it.key to f(it) }
//
//        val nextRelationList = nodeMap.flatMap { (i, node) ->
//            node.inferiors.map { j ->
//                nextElementMap[j]!! to nextElementMap[i]!!
//            } + node.superiors.map { s ->
//                nextElementMap[i]!! to nextElementMap[s]!!
//            }
//        }
//
//        return ConcretePartialOrderSet(
//            nextElementMap.values.toSet(),
//            nextRelationList,
//        )
    }

    override fun <W, Q : PartialOrder<W>> flatMap(f: (V) -> Q): PartialOrder<W>? {
        if (elements.isEmpty()) {
            return null
        }

        val nestedPartialOrder = map(f)
        val elementStack = Stack<PartialOrder<W>>()

        nestedPartialOrder.dfsPreAndPostfix(
            { entered ->
                if (entered != null) {
                    elementStack.push(entered)
                }
            }
        ) { exited ->
            if (exited != null && elementStack.isNotEmpty()) {
                val parent = elementStack.pop()
                val attachedParent = Orders.concat(parent, exited)
                elementStack.push(attachedParent)
            }
        }

        return elementStack.pop()
    }

    companion object {

        fun <K : Comparable<K>, V : KeyedValue<K>> ofChildMap(
            elementMap: Map<K, V>,
            childMap: Map<K, List<K>>,
        ) = KeyedPartialOrderSet(
            elementMap,
            Unit.run {
                val nodeMap = mutableMapOf<K, Pair<MutableSet<K>, MutableSet<K>>>()

                for ((inf, sups) in childMap) {
                    if (inf !in nodeMap) {
                        nodeMap[inf] = mutableSetOf<K>() to mutableSetOf()
                    }

                    val toSupSet = nodeMap[inf]!!.second
                    for (sup in sups) {
                        toSupSet.add(sup)

                        if (sup !in nodeMap) {
                            nodeMap[sup] = mutableSetOf<K>() to mutableSetOf()
                        }
                        nodeMap[sup]!!.first.add(inf)
                    }
                }

                val innerMap = nodeMap.mapValues { (k, pair) ->
                    val (infSet, supSet) = pair
                    KeyNode(k, infSet, supSet)
                }

                MaskedMap(innerMap, mutableSetOf()) { elt, masks ->
                    elt.copy(
                        inferiors = elt.inferiors - masks,
                        superiors = elt.superiors - masks,
                    )
                }
            }
        )

    }
}