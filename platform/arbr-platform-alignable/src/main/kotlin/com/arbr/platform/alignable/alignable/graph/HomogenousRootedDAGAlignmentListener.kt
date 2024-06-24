package com.arbr.platform.alignable.alignable.graph

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.alignable.alignable.collections.MapAlignmentListener
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation
import java.util.UUID

interface AlignmentListener {
    val uuid: String
}

/**
 * Alignment listener for AlignableHomogenousRootedDAG
 */
abstract class HomogenousRootedDAGAlignmentListener<E : Alignable<E, O>, O> : AlignmentListener {

    fun childTypeMapListenerProxy(): MapAlignmentListener<
            AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
            MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
            > {
        val parentUuid = uuid

        return object : MapAlignmentListener<
                AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
                MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
                >() {

            override val uuid: String = parentUuid

            override fun didEnterMap(map: AlignableMap<AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>, MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>>) {
                //
            }

            override fun didInsertElement(
                operationIndex: Int,
                key: String,
                element: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
            ) {
                didAddChildType(
                    operationIndex, key, element
                )
            }

            override fun didEditElement(
                operationIndex: Int,
                key: String,
                alignment: List<MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>>,
                fromElement: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
                mapElement: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
            ) {
                //
            }

            override fun didRemoveElement(
                operationIndex: Int,
                key: String,
                element: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
            ) {
                didRemoveChildType(
                    operationIndex, key, element
                )
            }

            override fun didExitMap(map: AlignableMap<AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>, MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>>) {
                //
            }
        }
    }

    fun subgraphListenerProxy(
        childKey: String,
    ): MapAlignmentListener<
            AlignableHomogenousRootedDAG<E, O>,
            HomogenousRootedDAGAlignmentOperation<E, O>
            > {
        val parentUuid = uuid

        return object : MapAlignmentListener<
                AlignableHomogenousRootedDAG<E, O>,
                HomogenousRootedDAGAlignmentOperation<E, O>
                >() {

            override val uuid = parentUuid

            override fun didEnterMap(map: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>) {
                didEnterSubgraphSequence(map, childKey)
            }

            override fun didInsertElement(
                operationIndex: Int,
                key: String,
                element: AlignableHomogenousRootedDAG<E, O>
            ) {
                didAddSubgraph(element, childKey, key)
            }

            override fun didEditElement(
                operationIndex: Int,
                key: String,
                alignment: List<HomogenousRootedDAGAlignmentOperation<E, O>>,
                fromElement: AlignableHomogenousRootedDAG<E, O>,
                mapElement: AlignableHomogenousRootedDAG<E, O>
            ) {
                //
            }

            override fun didRemoveElement(
                operationIndex: Int,
                key: String,
                element: AlignableHomogenousRootedDAG<E, O>
            ) {
                didRemoveSubgraph(element, childKey, key)
            }

            override fun didExitMap(map: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>) {
                didExitSubgraphSequence(map, childKey)
            }

        }
    }

    abstract fun didEnter(dag: AlignableHomogenousRootedDAG<E, O>)

    abstract fun didApplyNodeOperation(
        operationIndex: Int,
        operation: O,
        element: E,
    )

    abstract fun didAddChildType(
        operationIndex: Int,
        key: String,
        element: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
    )

    abstract fun didRemoveChildType(
        operationIndex: Int,
        key: String,
        element: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
    )

    abstract fun didEnterSubgraphSequence(
        map: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
        childKey: String,
    )

    abstract fun didAddSubgraph(subgraph: AlignableHomogenousRootedDAG<E, O>, childKey: String, key: String)

    abstract fun didRemoveSubgraph(subgraph: AlignableHomogenousRootedDAG<E, O>, childKey: String, key: String)

    abstract fun didExitSubgraphSequence(
        map: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
        childKey: String,
    )

    abstract fun didExit(dag: AlignableHomogenousRootedDAG<E, O>)

    companion object {
        fun <E : Alignable<E, O>, O> compoundListenerOf(listeners: List<HomogenousRootedDAGAlignmentListener<E, O>>) =
            object : HomogenousRootedDAGAlignmentListener<E, O>() {
                override val uuid: String = UUID.randomUUID().toString()

                override fun didEnter(dag: AlignableHomogenousRootedDAG<E, O>) {
                    listeners.forEach { it.didEnter(dag) }
                }

                override fun didEnterSubgraphSequence(
                    map: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
                    childKey: String
                ) {
                    listeners.forEach { it.didEnterSubgraphSequence(map, childKey) }
                }

                override fun didApplyNodeOperation(operationIndex: Int, operation: O, element: E) {
                    listeners.forEach { it.didApplyNodeOperation(operationIndex, operation, element) }
                }

                override fun didAddChildType(
                    operationIndex: Int,
                    key: String,
                    element: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
                ) {
                    listeners.forEach { it.didAddChildType(operationIndex, key, element) }
                }

                override fun didRemoveChildType(
                    operationIndex: Int,
                    key: String,
                    element: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
                ) {
                    listeners.forEach { it.didRemoveChildType(operationIndex, key, element) }
                }

                override fun didAddSubgraph(
                    subgraph: AlignableHomogenousRootedDAG<E, O>,
                    childKey: String,
                    key: String
                ) {
                    listeners.forEach { it.didAddSubgraph(subgraph, childKey, key) }
                }

                override fun didRemoveSubgraph(
                    subgraph: AlignableHomogenousRootedDAG<E, O>,
                    childKey: String,
                    key: String
                ) {
                    listeners.forEach { it.didRemoveSubgraph(subgraph, childKey, key) }
                }

                override fun didExitSubgraphSequence(
                    map: AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
                    childKey: String
                ) {
                    listeners.forEach { it.didExitSubgraphSequence(map, childKey) }
                }

                override fun didExit(dag: AlignableHomogenousRootedDAG<E, O>) {
                    listeners.forEach { it.didExit(dag) }
                }

            }
    }
}

