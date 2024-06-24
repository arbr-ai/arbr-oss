package com.arbr.platform.data_structures_common.partial_order

/**
 * Linear/List-Order as an implementor of PartialOrder
 */
open class LinearOrderList<V>(
    private val elementList: List<V>
): PartialOrder<V>, List<V> by elementList {
    override val elements: Collection<V> = elementList

    val relationList: List<Pair<V, V>> by lazy {
        elementList.dropLast(1).zip(elementList.drop(1))
    }

    private val elementReverseMap by lazy {
        elementList
            .withIndex()
            .associate { it.value to it.index }
    }

    override fun inferiorsOf(element: V): List<V> {
        val eltIndex = elementReverseMap[element] ?: return emptyList()
        return if (eltIndex > 0) {
            listOf(elementList[eltIndex - 1])
        } else {
            emptyList()
        }
    }

    override fun superiorsOf(element: V): List<V> {
        val eltIndex = elementReverseMap[element] ?: return emptyList()
        return if (eltIndex < elementList.size - 1) {
            listOf(elementList[eltIndex + 1])
        } else {
            emptyList()
        }
    }

    override fun minima(): List<V> {
        return elementList.take(1)
    }

    override fun maxima(): List<V> {
        return elementList.takeLast(1)
    }

    override fun pushAllRelationships(relations: List<Pair<V, V>>): LinearOrderList<V> {
        // TODO: Consider throwing or sorting if non-linear pair
        return this
    }

    override fun pushAll(elements: Collection<V>): LinearOrderList<V> {
        return LinearOrderList(elements + elementList)
    }

    override fun popAll(elementsToRemove: Collection<V>): LinearOrderList<V> {
        return LinearOrderList(elementList.filter { it !in elementsToRemove })
    }

    override fun pop(element: V): LinearOrderList<V> {
        if (elementList.firstOrNull() != element) {
            throw NoSuchElementException()
        }

        val sublist = elementList.subList(1, elementList.size)
        return LinearOrderList(sublist)
    }

    /**
     * Pop the minimal elements from the partial order and return the corresponding partial orders.
     */
    override fun popMinima(): List<Pair<V, LinearOrderList<V>>> {
        return minima().map {
            it to pop(it)
        }
    }

    override fun <W> map(f: (V) -> W): LinearOrderList<W> {
        return LinearOrderList(
            elementList.map(f)
        )
    }

    override fun <W, Q : PartialOrder<W>> flatMap(f: (V) -> Q): PartialOrder<W>? {
        if (elementList.isEmpty()) {
            return null
        }

        return Orders.concatAll(
            f(elementList.first()),
            elementList.drop(1).map(f),
        )
    }

    override fun contains(element: V): Boolean {
        return elementList.contains(element)
    }

    override fun dfsPreAndPostfix(pre: (V?) -> Unit, post: (V?) -> Unit) {
        pre(null)
        for (v in elementList) {
            pre(v)
            post(v)
        }
        post(null)
    }

    override fun bfs(f: (V) -> Unit) {
        for (v in elementList) {
            f(v)
        }
    }

    override fun toString(): String {
        return elementList.toString()
    }
}
