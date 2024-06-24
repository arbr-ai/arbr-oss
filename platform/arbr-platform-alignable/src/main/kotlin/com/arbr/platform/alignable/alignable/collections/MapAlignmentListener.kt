package com.arbr.platform.alignable.alignable.collections

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.graph.AlignmentListener
import java.util.*

abstract class MapAlignmentListener<E : Alignable<E, O>, O>: AlignmentListener {

    abstract fun didEnterMap(
        map: AlignableMap<E, O>,
    )

    abstract fun didInsertElement(
        operationIndex: Int,
        key: String,
        element: E,
    )

    abstract fun didEditElement(
        operationIndex: Int,
        key: String,
        alignment: List<O>,
        fromElement: E,
        mapElement: E,
    )

    abstract fun didRemoveElement(
        operationIndex: Int,
        key: String,
        element: E,
    )

    abstract fun didExitMap(
        map: AlignableMap<E, O>,
    )

    companion object {
        fun <E : Alignable<E, O>, O> compoundListenerOf(listeners: List<MapAlignmentListener<E, O>>) = object : MapAlignmentListener<E, O>() {
            override val uuid: String = UUID.randomUUID().toString()

            override fun didEnterMap(map: AlignableMap<E, O>) {
                listeners.forEach { it.didEnterMap(map) }
            }

            override fun didInsertElement(operationIndex: Int, key: String, element: E) {
                listeners.forEach { it.didInsertElement(operationIndex, key, element) }
            }

            override fun didEditElement(
                operationIndex: Int,
                key: String,
                alignment: List<O>,
                fromElement: E,
                mapElement: E
            ) {
                listeners.forEach { it.didEditElement(operationIndex, key, alignment, fromElement, mapElement) }
            }

            override fun didRemoveElement(operationIndex: Int, key: String, element: E) {
                listeners.forEach { it.didRemoveElement(operationIndex, key, element) }
            }

            override fun didExitMap(map: AlignableMap<E, O>) {
                listeners.forEach { it.didExitMap(map) }
            }
        }
    }
}
