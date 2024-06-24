package com.arbr.platform.alignable.alignable.collections

import com.arbr.content_formats.mapper.Mappers
import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.platform.data_structures_common.immutable.immutableLinkedMapOf

/**
 * Alignable Map<String, E>. Could add enumerated key types in the future.
 */
open class AlignableMap<E : Alignable<E, O>, O>(
    val elements: ImmutableLinkedMap<String, E>,
) : Alignable<AlignableMap<E, O>, MapAlignmentOperation<E, O>>, Map<String, E> by elements {
    protected val listeners: MutableList<MapAlignmentListener<E, O>> = mutableListOf()

    fun addListener(listener: MapAlignmentListener<E, O>) {
        if (!listeners.any { it.uuid == listener.uuid }) {
            listeners.add(listener)
        }
    }

    override fun align(e: AlignableMap<E, O>): Alignment<AlignableMap<E, O>, MapAlignmentOperation<E, O>> {
        val keys = elements.keys.union(e.elements.keys)

        val operations: List<MapAlignmentOperation<E, O>> = keys.flatMap { k ->
            val thisValue = elements[k]
            val otherValue = e.elements[k]
            if (thisValue == null && otherValue == null) {
                // Technically a bad state but safe to cover anyway
                emptyList()
            } else if (thisValue == null) {
                val empty = otherValue!!.empty()
                val emptyEditAlignment = empty.align(otherValue)
                if (emptyEditAlignment.operations.isEmpty()) {
                    listOf(
                        // Just insert, no edit needed
                        MapAlignmentOperation.Insert(
                            k,
                            emptyList(),
                            empty,
                            1.0,
                        ),
                    )
                } else {
                    listOf(
                        MapAlignmentOperation.Insert(
                            k,
                            emptyList(),
                            empty,
                            1.0,
                        ),
                        MapAlignmentOperation.Edit(
                            k,
                            emptyEditAlignment.operations,
                            empty,
                            otherValue,
                            emptyEditAlignment.cost,
                        ),
                    )
                }
            } else if (otherValue == null) {
                val empty = thisValue.empty()
                val emptyEditAlignment = thisValue.align(empty)
                if (emptyEditAlignment.operations.isEmpty()) {
                    // Just delete, no edit needed
                    listOf(
                        MapAlignmentOperation.Delete(
                            k,
                            emptyList(),
                            empty,
                            1.0,
                        ),
                    )
                } else {
                    listOf(
                        MapAlignmentOperation.Edit(
                            k,
                            emptyEditAlignment.operations,
                            thisValue,
                            empty,
                            emptyEditAlignment.cost,
                        ),
                        MapAlignmentOperation.Delete(
                            k,
                            emptyList(),
                            empty,
                            1.0,
                        ),
                    )
                }
            } else {
                when (val innerAlignment = thisValue.align(otherValue)) {
                    is Alignment.Align -> listOf(
                        MapAlignmentOperation.Edit(
                            k,
                            innerAlignment.operations,
                            thisValue,
                            otherValue,
                            innerAlignment.cost,
                        )
                    )

                    is Alignment.Equal -> emptyList()
                }
            }
        }

        return if (operations.isEmpty()) {
            Alignment.Equal(
                this,
                e,
            )
        } else {
            val cost = operations.map { it.cost }.reduce { a, b -> a + b }

            Alignment.Align(
                operations,
                cost,
                this,
                e,
            )
        }
    }

    override fun empty(): AlignableMap<E, O> {
        return AlignableMap(immutableLinkedMapOf())
    }

    override fun applyAlignment(alignmentOperations: List<MapAlignmentOperation<E, O>>): AlignableMap<E, O> {
        val compoundListener = MapAlignmentListener.compoundListenerOf(listeners)
        compoundListener.didEnterMap(this)
        val elementMap = LinkedHashMap<String, E>().also { lhm ->
            lhm.putAll(elements)
        }
        for ((i, op) in alignmentOperations.withIndex()) {
            val key = op.atKey
            when (op) {
                is MapAlignmentOperation.Delete -> {
                    elementMap.remove(key)
                    compoundListener.didRemoveElement(i, op.atKey, op.element)
                }

                is MapAlignmentOperation.Edit -> elementMap.compute(key) { _, e ->
                    // Could technically just take the final element here, but replaying enables listeners and such
                    (e?.applyAlignment(op.alignment) ?: op.element)
                        .also {
                            compoundListener.didEditElement(i, op.atKey, op.alignment, op.fromElement, op.element)
                        }
                }

                is MapAlignmentOperation.Insert -> {
                    val targetElement = op.element
                    elementMap[key] = targetElement
                    compoundListener.didInsertElement(i, op.atKey, op.element)
                }
            }
        }

        return AlignableMap(ImmutableLinkedMap.ofLinkedHashMap(elementMap))
            .also { compoundListener.didExitMap(it) }
    }

    override fun toString(): String {
        return yamlMapper.writeValueAsString(elements)
    }

    companion object {
        private val yamlMapper = Mappers.yamlMapper
    }
}