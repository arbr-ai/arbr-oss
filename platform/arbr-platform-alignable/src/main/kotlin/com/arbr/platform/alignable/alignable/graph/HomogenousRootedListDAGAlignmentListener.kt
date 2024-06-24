package com.arbr.platform.alignable.alignable.graph

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.collections.*
import java.util.UUID

/**
 * Alignment listener for AlignableHomogenousRootedListDAG
 */
abstract class HomogenousRootedListDAGAlignmentListener<E : Alignable<E, O>, O> : AlignmentListener {

    fun childTypeMapListenerProxy(): MapAlignmentListener<
            AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
            SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
            > {
        val parentUuid = uuid

        return object : MapAlignmentListener<
                AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
                SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
                >() {
            override val uuid: String = parentUuid

            override fun didEnterMap(map: AlignableMap<AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>, SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>>) {
                //
            }

            override fun didInsertElement(
                operationIndex: Int,
                key: String,
                element: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
            ) {
                didAddChildType(
                    operationIndex, key, element
                )
            }

            override fun didEditElement(
                operationIndex: Int,
                key: String,
                alignment: List<SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>>,
                fromElement: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
                mapElement: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
            ) {
                //
            }

            override fun didRemoveElement(
                operationIndex: Int,
                key: String,
                element: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
            ) {
                didRemoveChildType(
                    operationIndex, key, element
                )
            }

            override fun didExitMap(map: AlignableMap<AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>, SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>>) {
                //
            }
        }
    }

    fun subgraphListenerProxy(
        childKey: String,
    ): SequenceAlignmentListener<
            AlignableHomogenousRootedListDAG<E, O>,
            HomogenousRootedListDAGAlignmentOperation<E, O>
            > {
        val parentUuid = uuid

        return object : SequenceAlignmentListener<
                AlignableHomogenousRootedListDAG<E, O>,
                HomogenousRootedListDAGAlignmentOperation<E, O>
                >() {

            override val uuid = parentUuid

            override fun didEnterSequence(sequence: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>) {
                didEnterSubgraphSequence(sequence, childKey)
            }

            override fun didAddElement(
                operationIndex: Int,
                targetElementIndex: Int,
                element: AlignableHomogenousRootedListDAG<E, O>
            ) {
                didAddSubgraph(element, childKey, targetElementIndex)
            }

            override fun didEditElement(
                operationIndex: Int,
                targetElementIndex: Int,
                fromElement: AlignableHomogenousRootedListDAG<E, O>,
                sequenceElement: AlignableHomogenousRootedListDAG<E, O>
            ) {
                //
            }

            override fun didRemoveElement(
                operationIndex: Int,
                targetElementIndex: Int,
                element: AlignableHomogenousRootedListDAG<E, O>
            ) {
                didRemoveSubgraph(element, childKey, targetElementIndex)
            }

            override fun didExitSequence(sequence: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>) {
                didExitSubgraphSequence(sequence, childKey)
            }

        }
    }

    abstract fun didEnter(dag: AlignableHomogenousRootedListDAG<E, O>)

    abstract fun didApplyNodeOperation(
        operationIndex: Int,
        operation: O,
        element: E,
    )

    abstract fun didAddChildType(
        operationIndex: Int,
        key: String,
        element: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
    )

    abstract fun didRemoveChildType(
        operationIndex: Int,
        key: String,
        element: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
    )

    abstract fun didEnterSubgraphSequence(
        map: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
        childKey: String,
    )

    abstract fun didAddSubgraph(subgraph: AlignableHomogenousRootedListDAG<E, O>, childKey: String, atIndex: Int)

    abstract fun didRemoveSubgraph(subgraph: AlignableHomogenousRootedListDAG<E, O>, childKey: String, atIndex: Int)

    abstract fun didExitSubgraphSequence(
        map: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
        childKey: String,
    )

    abstract fun didExit(dag: AlignableHomogenousRootedListDAG<E, O>)

    companion object {
        fun <E : Alignable<E, O>, O> compoundListenerOf(listeners: List<HomogenousRootedListDAGAlignmentListener<E, O>>) =
            object : HomogenousRootedListDAGAlignmentListener<E, O>() {
                override val uuid: String = UUID.randomUUID().toString()

                override fun didEnter(dag: AlignableHomogenousRootedListDAG<E, O>) {
                    listeners.forEach { it.didEnter(dag) }
                }

                override fun didEnterSubgraphSequence(
                    map: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
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
                    element: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
                ) {
                    listeners.forEach { it.didAddChildType(operationIndex, key, element) }
                }

                override fun didRemoveChildType(
                    operationIndex: Int,
                    key: String,
                    element: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
                ) {
                    listeners.forEach { it.didRemoveChildType(operationIndex, key, element) }
                }

                override fun didAddSubgraph(
                    subgraph: AlignableHomogenousRootedListDAG<E, O>,
                    childKey: String,
                    atIndex: Int
                ) {
                    listeners.forEach { it.didAddSubgraph(subgraph, childKey, atIndex) }
                }

                override fun didRemoveSubgraph(
                    subgraph: AlignableHomogenousRootedListDAG<E, O>,
                    childKey: String,
                    atIndex: Int
                ) {
                    listeners.forEach { it.didRemoveSubgraph(subgraph, childKey, atIndex) }
                }

                override fun didExitSubgraphSequence(
                    map: AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
                    childKey: String
                ) {
                    listeners.forEach { it.didExitSubgraphSequence(map, childKey) }
                }

                override fun didExit(dag: AlignableHomogenousRootedListDAG<E, O>) {
                    listeners.forEach { it.didExit(dag) }
                }

            }
    }
}
