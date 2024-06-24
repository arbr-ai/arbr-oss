package com.arbr.platform.alignable.alignable.collections

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.graph.AlignmentListener
import java.util.*

abstract class KeyValueAlignmentListener<K, E : Alignable<E, O>, O>: AlignmentListener {

    abstract fun didEnterMap(
        map: AlignableKeyValue<K, E, O>,
    )

    abstract fun didInsertElement(
        operationIndex: Int,
        key: K,
        element: E,
    )

    abstract fun didEditElement(
        operationIndex: Int,
        key: K,
        alignment: List<O>,
        fromElement: E,
        mapElement: E,
    )

    abstract fun didRemoveElement(
        operationIndex: Int,
        key: K,
        element: E,
    )

    abstract fun didExitMap(
        map: AlignableKeyValue<K, E, O>,
    )

    companion object {
        fun <K, E : Alignable<E, O>, O> compoundListenerOf(
            listeners: List<KeyValueAlignmentListener<K, E, O>>
        ) = object : KeyValueAlignmentListener<K, E, O>() {
            override val uuid: String = UUID.randomUUID().toString()

            override fun didEnterMap(map: AlignableKeyValue<K, E, O>) {
                listeners.forEach { it.didEnterMap(map) }
            }

            override fun didInsertElement(operationIndex: Int, key: K, element: E) {
                listeners.forEach { it.didInsertElement(operationIndex, key, element) }
            }

            override fun didEditElement(
                operationIndex: Int,
                key: K,
                alignment: List<O>,
                fromElement: E,
                mapElement: E
            ) {
                listeners.forEach { it.didEditElement(operationIndex, key, alignment, fromElement, mapElement) }
            }

            override fun didRemoveElement(operationIndex: Int, key: K, element: E) {
                listeners.forEach { it.didRemoveElement(operationIndex, key, element) }
            }

            override fun didExitMap(map: AlignableKeyValue<K, E, O>) {
                listeners.forEach { it.didExitMap(map) }
            }
        }
    }
}
