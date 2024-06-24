package com.arbr.platform.alignable.alignable.graph

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.collections.AlignableList
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation
import com.arbr.platform.alignable.alignable.collections.SequenceAlignmentOperation


sealed class HomogenousRootedListDAGAlignmentOperation<E : Alignable<E, O>, O> {

    class Node<E : Alignable<E, O>, O>(
        val nodeOperation: O
    ): HomogenousRootedListDAGAlignmentOperation<E, O>()

    class ChildType<E : Alignable<E, O>, O>(
        val childTypeOperation: MapAlignmentOperation<
                AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
                SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
                >
    ): HomogenousRootedListDAGAlignmentOperation<E, O>()
}
