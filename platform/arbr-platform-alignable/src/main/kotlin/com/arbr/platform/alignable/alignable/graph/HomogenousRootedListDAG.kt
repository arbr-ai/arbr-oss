package com.arbr.platform.alignable.alignable.graph

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.collections.AlignableList
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap

interface HomogenousRootedListDAG<T> {
    val nodeValue: T

    // Children arranged by "type"
    // Maybe consider a k-v map
    val children: Map<String, List<HomogenousRootedListDAG<T>>>

    /**
     * Map in DFS order.
     * Consider enforcing no cycles here
     */
    fun <U> map(f: (T) -> U): HomogenousRootedListDAG<U> {
        return ConcreteHomogenousRootedListDAG(
            f(nodeValue),
            children.mapValues { (_, type) ->
                type.map { it.map(f) }
            }
        )
    }

    fun <U> lazyMap(f: (T) -> U): HomogenousRootedListDAG<U> {
        val thisNodeValue = nodeValue
        val theseChildren = children

        return object : HomogenousRootedListDAG<U> {
            override val nodeValue: U by lazy { f(thisNodeValue) }
            override val children: Map<String, List<HomogenousRootedListDAG<U>>> by lazy {
                theseChildren.mapValues { (_, type) ->
                    type.map { it.lazyMap(f) }
                }
            }
        }
    }

    private fun <U> mapWithParent(f: (T, U?) -> U, parent: U?): HomogenousRootedListDAG<U> {
        val mappedNodeValue = f(nodeValue, parent)
        return ConcreteHomogenousRootedListDAG(
            mappedNodeValue,
            children.mapValues { (_, type) ->
                type.map { it.mapWithParent(f, mappedNodeValue) }
            }
        )
    }

    /**
     * Map in DFS order.
     * Consider enforcing no cycles here
     */
    fun <U> mapWithParent(f: (T, U?) -> U): HomogenousRootedListDAG<U> {
        return mapWithParent(f, null)
    }

    fun <U> mapWithParentAndPriorSibling(f: (T, U?, U?) -> U, parent: U?, priorSibling: U?): HomogenousRootedListDAG<U> {
        val mappedNodeValue = f(nodeValue, parent, priorSibling)
        return ConcreteHomogenousRootedListDAG(
            mappedNodeValue,
            children.mapValues { (_, childList) ->
                var mappedPriorSibling: U? = null
                childList.map {
                    it.mapWithParentAndPriorSibling(f, mappedNodeValue, mappedPriorSibling)
                        .also { mappedPriorSiblingGraph ->
                            mappedPriorSibling = mappedPriorSiblingGraph.nodeValue
                        }
                }
            }
        )
    }

    fun <U> mapWithParentAndPriorSibling(f: (T, U?, U?) -> U): HomogenousRootedListDAG<U> {
        return mapWithParentAndPriorSibling(f, null, null)
    }

    /**
     * Map pre and post children
     */
    fun <U, V> mapPrePost(
        f: (T) -> U,
        g: (U, Map<String, List<HomogenousRootedListDAG<V>>>) -> V // post-map with children
    ): HomogenousRootedListDAG<V> {
        val premappedNode = f(nodeValue)
        val mappedChildren: Map<String, List<HomogenousRootedListDAG<V>>> = children.mapValues { (_, type) ->
            type.map { it.mapPrePost(f, g) }
        }
        val postNode = g(premappedNode, mappedChildren)

        return ConcreteHomogenousRootedListDAG(
            postNode,
            mappedChildren
        )
    }

    /**
     * Flatten nodes to a list in DFS order.
     */
    fun flatten(
        seen: MutableSet<T>,
    ): List<T> {
        if (nodeValue in seen) {
            return emptyList()
        }
        seen.add(nodeValue)

        return listOf(nodeValue) + children.flatMap { l ->
            l.value.flatMap { subgraph ->
                if (subgraph.nodeValue in seen) {
                    emptyList()
                } else {
                    subgraph.flatten(seen)
                }
            }
        }
    }

    /**
     * Flatten nodes to a list in DFS-entry order.
     */
    fun flatten(): List<T> {
        return flatten(mutableSetOf())
    }

    fun <E : Alignable<E, O>, O> flatMapAlignable(f: (T) -> E): AlignableHomogenousRootedListDAG<E, O> {
        return AlignableHomogenousRootedListDAGImpl(
            f(nodeValue),
            AlignableMap(
                ImmutableLinkedMap.ofLinkedHashMap(
                    LinkedHashMap<String, AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>>().also { lhm ->
                        lhm.putAll(
                            children.mapValues { (_, type) ->
                                AlignableList(type.map { it.flatMapAlignable(f) })
                            }
                        )
                    }
                )
            )
        )
    }

    companion object {
        fun <G, T> fromGenerator(
            generator: G,
            getNodeValue: (G) -> T,
            getChildGenerators: (G) -> Map<String, List<G>>,
        ): HomogenousRootedListDAG<T> {
            return ConcreteHomogenousRootedListDAG(
                getNodeValue(generator),
                getChildGenerators(generator).mapValues { (_, fkList) ->
                    fkList.map {
                        fromGenerator(it, getNodeValue, getChildGenerators)
                    }
                }
            )
        }
    }
}