package com.arbr.platform.alignable.alignable.collections

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.graph.AlignmentListener
import java.util.*

abstract class SequenceAlignmentListener<E : Alignable<E, O>, O>: AlignmentListener {

    abstract fun didEnterSequence(
        sequence: AlignableList<E, O>,
    )

    abstract fun didAddElement(
        operationIndex: Int,
        targetElementIndex: Int,
        element: E,
    )

    abstract fun didEditElement(
        operationIndex: Int,
        targetElementIndex: Int,
        fromElement: E,
        sequenceElement: E,
    )

    abstract fun didRemoveElement(
        operationIndex: Int,
        targetElementIndex: Int,
        element: E,
    )

    abstract fun didExitSequence(
        sequence: AlignableList<E, O>,
    )

    companion object {
        fun <E : Alignable<E, O>, O> compoundListenerOf(listeners: List<SequenceAlignmentListener<E, O>>) = object : SequenceAlignmentListener<E, O>() {
            override val uuid: String = UUID.randomUUID().toString()

            override fun didEnterSequence(sequence: AlignableList<E, O>) {
                listeners.forEach { it.didEnterSequence(sequence) }
            }

            override fun didAddElement(operationIndex: Int, targetElementIndex: Int, element: E) {
                listeners.forEach { it.didAddElement(operationIndex, targetElementIndex, element) }
            }

            override fun didEditElement(
                operationIndex: Int,
                targetElementIndex: Int,
                fromElement: E,
                sequenceElement: E
            ) {
                listeners.forEach { it.didEditElement(operationIndex, targetElementIndex, fromElement, sequenceElement) }
            }

            override fun didRemoveElement(operationIndex: Int, targetElementIndex: Int, element: E) {
                listeners.forEach { it.didRemoveElement(operationIndex, targetElementIndex, element) }
            }

            override fun didExitSequence(sequence: AlignableList<E, O>) {
                listeners.forEach { it.didExitSequence(sequence) }
            }
        }
    }
}
