package com.arbr.platform.data_structures_common.partial_order


enum class PartialOrderFlatteningScheme {
    BREADTH_FIRST,
    DEPTH_FIRST,
}

sealed interface PartialOrder<V> {

    /**
     * Elements of the partial order.
     */
    val elements: Collection<V>

    /**
     * Get immediate children of the element according to the relation, i.e. for element a, all b s.t. (a, b) \in R
     * Not required to be transitive
     */
    fun superiorsOf(element: V): List<V>

    /**
     * Get immediate parents of the element according to the relation, i.e. for element b, all a s.t. (a, b) \in R
     * Not required to be transitive
     */
    fun inferiorsOf(element: V): List<V>

    /**
     * Whether the given element is represented in any relation pair.
     */
    fun contains(element: V): Boolean {
        return elements.contains(element)
    }

    /**
     * Get the minimal elements from the partial order.
     */
    fun minima(): Collection<V>

    /**
     * Get the maximal elements from the partial order.
     */
    fun maxima(): Collection<V>

    /**
     * Optional comparator on viable elements.
     */
    val elementSoftOrder: Comparator<V>?
        get() = null

    /**
     * Pop all elements from set and their relationships - does not check if it is minimal
     */
    fun popAll(elementsToRemove: Collection<V>): PartialOrder<V>

    /**
     * Pop a single element - does not check if it is minimal
     */
    fun pop(element: V): PartialOrder<V>

    fun pushAll(elements: Collection<V>): PartialOrder<V>

    fun push(element: V): PartialOrder<V> = pushAll(listOf(element))

    /**
     * Pop the minimal elements from the partial order and return the corresponding partial orders.
     */
    fun popMinima(): List<Pair<V, PartialOrder<V>>> {
        return minima().map {
            it to pop(it)
        }
    }

    fun pushAllRelationships(relations: List<Pair<V, V>>): PartialOrder<V>

    fun pushRelationship(inferior: V, superior: V): PartialOrder<V> = pushAllRelationships(listOf(inferior to superior))

    fun <W> map(f: (V) -> W): PartialOrder<W>

    /**
     * flatMap to a potentially different kind of partial order, respecting order among sub-orders as well order
     * interior to each sub-order
     * Returns null if this order is empty
     */
    fun <W, Q : PartialOrder<W>> flatMap(f: (V) -> Q): PartialOrder<W>?

    /**
     * Prefix and postfix-order DFS
     */
    fun dfsPreAndPostfix(pre: (V?) -> Unit, post: (V?) -> Unit)

    /**
     * Prefix-order DFS
     */
    fun dfs(f: (V?) -> Unit) {
        dfsPreAndPostfix(f) {}
    }

    /**
     * Postfix-order DFS
     */
    fun dfsPostfix(f: (V?) -> Unit) {
        dfsPreAndPostfix({}, f)
    }

    // TODO: Seems to have a bug in at least one spot where DFS does not
    fun bfs(f: (V) -> Unit)

    /**
     * Flatten to a list while respecting viability.
     * Optionally specify a scheme for topological flattening.
     */
    fun toFlatList(scheme: PartialOrderFlatteningScheme = PartialOrderFlatteningScheme.BREADTH_FIRST): List<V> {
        val listRepr = mutableListOf<V>()
        when (scheme) {
            PartialOrderFlatteningScheme.BREADTH_FIRST -> bfs {
                listRepr.add(it)
            }

            PartialOrderFlatteningScheme.DEPTH_FIRST -> dfs {
                if (it != null) {
                    listRepr.add(it)
                }
            }
        }
        return listRepr
    }

    fun findCycles(): List<List<V>> {
        val cycles = mutableSetOf<List<V>>()

        var paths = mutableListOf<List<V>>()
        paths.addAll(
            elements.map { listOf(it) }
        )

        while (paths.isNotEmpty()) {
            val nextPaths = mutableListOf<List<V>>()
            for (path in paths) {
                val children = superiorsOf(path.last())
                for (c in children) {
                    if (c in path) {
                        val canonicalStartIndex = path
                            .withIndex()
                            .minBy { (i, v) -> v.hashCode() }
                            .index

                        val orderedCycle = path.drop(canonicalStartIndex) + path.take(canonicalStartIndex)

                        cycles.add(orderedCycle)
                    } else {
                        nextPaths.add(path + c)
                    }
                }
            }

            paths = nextPaths
        }

        return cycles.toList()
    }

    fun asPartialOrderSet(): PartialOrderSet<V> {
        return if (this is PartialOrderSet) {
            this
        } else {
            // Compute relation as list
            val relationList = elements.flatMap {
                superiorsOf(it).map { ch -> it to ch }
            }.distinct()

            ConcretePartialOrderSet(
                immutableLinkedSetOf(elements),
                relationList,
                elementSoftOrder,
            )
        }
    }

    fun asConcretePartialOrderSet(): ConcretePartialOrderSet<V> {
        return if (this is ConcretePartialOrderSet) {
            this
        } else {
            // Compute relation as list
            val relationList = elements.flatMap {
                superiorsOf(it).map { ch -> it to ch }
            }.distinct()

            ConcretePartialOrderSet(
                immutableLinkedSetOf(elements),
                relationList,
                elementSoftOrder,
            )
        }
    }

    fun asLinearOrderList(): LinearOrderList<V> {
        return if (this is LinearOrderList) {
            this
        } else {
            val eltList = toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST)
            LinearOrderList(eltList)
        }
    }
    
}
