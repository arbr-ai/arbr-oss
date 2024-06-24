package com.arbr.platform.alignable.alignable.graph

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation


sealed class HomogenousRootedDAGAlignmentOperation<E : Alignable<E, O>, O> {

    class Node<E : Alignable<E, O>, O>(
        val nodeOperation: O
    ): HomogenousRootedDAGAlignmentOperation<E, O>()

    class ChildType<E : Alignable<E, O>, O>(
        val childTypeOperation: MapAlignmentOperation<
                AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
                MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
                >
    ): HomogenousRootedDAGAlignmentOperation<E, O>()
}
