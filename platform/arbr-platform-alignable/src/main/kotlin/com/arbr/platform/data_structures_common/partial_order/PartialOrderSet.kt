package com.arbr.platform.data_structures_common.partial_order

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedSet
import java.util.*

fun <V> emptyPoset(): PartialOrderSet<V> = ConcretePartialOrderSet<V>(
    emptySet(),
    emptyList(),
)

fun <V> singletonPoset(element: V): PartialOrderSet<V> = ConcretePartialOrderSet<V>(
    setOf(element),
    emptyList(),
)

fun <V> immutableLinkedSetOf(elements: Collection<V>): ImmutableLinkedSet<V> {
    return if (elements is ImmutableLinkedSet<V>) {
        elements
    } else {
        ImmutableLinkedSet(elements)
    }
}

fun <V> posetOf(elements: Collection<V>, relationList: List<Pair<V, V>>): PartialOrderSet<V> = ConcretePartialOrderSet(
    immutableLinkedSetOf(elements),
    relationList,
)

fun <V> posetOf(
    elements: Collection<V>,
    relationList: List<Pair<V, V>>,
    elementSoftOrder: kotlin.Comparator<V>?,
): PartialOrderSet<V> = ConcretePartialOrderSet(
    immutableLinkedSetOf(elements),
    relationList,
    elementSoftOrder,
)

interface PartialOrderSet<V> : PartialOrder<V> {

    override fun pop(element: V): PartialOrderSet<V>

    override fun popMinima(): List<Pair<V, PartialOrderSet<V>>>

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

    /**
     * Prefix and postfix-order DFS
     */
    override fun dfsPreAndPostfix(pre: (V?) -> Unit, post: (V?) -> Unit) {
        val added = mutableSetOf<V>()

        val stack = Stack<Pair<V?, (() -> Unit)?>>()
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

    override fun bfs(f: (V) -> Unit) {
        val seen = mutableSetOf<V>()

        var posetFrontier = listOf(this)

        while (posetFrontier.isNotEmpty()) {
            val minimaAndSubPosets = posetFrontier.flatMap { it.popMinima() }

            val nextPosetFrontier = mutableListOf<PartialOrderSet<V>>()
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
