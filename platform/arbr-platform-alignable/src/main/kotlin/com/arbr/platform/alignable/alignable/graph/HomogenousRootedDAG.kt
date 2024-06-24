package com.arbr.platform.alignable.alignable.graph

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap

interface HomogenousRootedDAG<T> {
    val nodeValue: T

    // Children arranged by "type"
    // Maybe consider a k-v map
    val children: Map<String, Map<String, HomogenousRootedDAG<T>>>

    /**
     * Map in DFS order.
     * Consider enforcing no cycles here
     */
    fun <U> map(f: (T) -> U): HomogenousRootedDAG<U> {
        return ConcreteHomogenousRootedDAG(
            f(nodeValue),
            children.mapValues { (_, type) ->
                type.mapValues { it.value.map(f) }
            }
        )
    }

    /**
     * Map pre and post children
     */
    fun <U, V> mapPrePost(
        f: (T) -> U,
        g: (U, Map<String, Map<String, HomogenousRootedDAG<V>>>) -> V // post-map with children
    ): HomogenousRootedDAG<V> {
        val premappedNode = f(nodeValue)
        val mappedChildren: Map<String, Map<String, HomogenousRootedDAG<V>>> = children.mapValues { (_, type) ->
            type.mapValues { it.value.mapPrePost(f, g) }
        }
        val postNode = g(premappedNode, mappedChildren)

        return ConcreteHomogenousRootedDAG(
            postNode,
            mappedChildren
        )
    }

    fun <U> lazyMap(f: (T) -> U): HomogenousRootedDAG<U> {
        val thisNodeValue = nodeValue
        val theseChildren = children

        return object : HomogenousRootedDAG<U> {
            override val nodeValue: U by lazy { f(thisNodeValue) }
            override val children: Map<String, Map<String, HomogenousRootedDAG<U>>> by lazy {
                theseChildren.mapValues { (_, type) ->
                    type.mapValues { it.value.lazyMap(f) }
                }
            }
        }
    }

    private fun <U> mapWithParent(f: (T, U?) -> U, parent: U?): HomogenousRootedDAG<U> {
        val mappedNodeValue = f(nodeValue, parent)
        return ConcreteHomogenousRootedDAG(
            mappedNodeValue,
            children.mapValues { (_, type) ->
                type.mapValues { it.value.mapWithParent(f, mappedNodeValue) }
            }
        )
    }

    /**
     * Map in DFS order.
     * Consider enforcing no cycles here
     */
    fun <U> mapWithParent(f: (T, U?) -> U): HomogenousRootedDAG<U> {
        return mapWithParent(f, null)
    }

    /**
     * Flatten nodes to a list in DFS order.
     */
    fun flatten(): List<T> {
        return listOf(nodeValue) + children.flatMap { l -> l.value.flatMap { it.value.flatten() } }
    }

    fun <E : Alignable<E, O>, O> flatMapAlignable(f: (T) -> E): AlignableHomogenousRootedDAG<E, O> {
        return AlignableHomogenousRootedDAGImpl(
            f(nodeValue),
            AlignableMap(
                ImmutableLinkedMap.ofLinkedHashMap(
                    LinkedHashMap<String, AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>>().also { lhm ->
                        lhm.putAll(
                            children.mapValues { (_, type) ->
                                AlignableMap(
                                    ImmutableLinkedMap.ofLinkedHashMap(
                                        LinkedHashMap<String, AlignableHomogenousRootedDAG<E, O>>().also { lhmi ->
                                            lhmi.putAll(type.mapValues { it.value.flatMapAlignable(f) })
                                        }
                                    )
                                )
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
            getChildGenerators: (G) -> Map<String, Map<String, G>>,
        ): HomogenousRootedDAG<T> {
            return ConcreteHomogenousRootedDAG(
                getNodeValue(generator),
                getChildGenerators(generator).mapValues { (_, fkList) ->
                    fkList.mapValues {
                        fromGenerator(it.value, getNodeValue, getChildGenerators)
                    }
                }
            )
        }
    }
}