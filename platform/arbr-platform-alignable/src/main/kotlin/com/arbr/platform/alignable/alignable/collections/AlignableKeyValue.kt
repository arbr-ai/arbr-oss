package com.arbr.platform.alignable.alignable.collections

import com.arbr.content_formats.mapper.Mappers
import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.platform.data_structures_common.immutable.immutableLinkedMapOf

/**
 * Alignable Map<String, E>. Could add enumerated key types in the future.
 */
open class AlignableKeyValue<K, E : Alignable<E, O>, O>(
    val elements: ImmutableLinkedMap<K, E>,
) : Alignable<AlignableKeyValue<K, E, O>, KeyValueAlignmentOperation<K, E, O>>, Map<K, E> by elements {
    protected val listeners: MutableList<KeyValueAlignmentListener<K, E, O>> = mutableListOf()

    fun addListener(listener: KeyValueAlignmentListener<K, E, O>) {
        if (!listeners.any { it.uuid == listener.uuid }) {
            listeners.add(listener)
        }
    }

    override fun align(e: AlignableKeyValue<K, E, O>): Alignment<AlignableKeyValue<K, E, O>, KeyValueAlignmentOperation<K, E, O>> {
        val keys = elements.keys.union(e.elements.keys)

        val operations: List<KeyValueAlignmentOperation<K, E, O>> = keys.flatMap { k ->
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
                        KeyValueAlignmentOperation.Insert(
                            k,
                            emptyList(),
                            empty,
                            1.0,
                        ),
                    )
                } else {
                    listOf(
                        KeyValueAlignmentOperation.Insert(
                            k,
                            emptyList(),
                            empty,
                            1.0,
                        ),
                        KeyValueAlignmentOperation.Edit(
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
                        KeyValueAlignmentOperation.Delete(
                            k,
                            emptyList(),
                            empty,
                            1.0,
                        ),
                    )
                } else {
                    listOf(
                        KeyValueAlignmentOperation.Edit(
                            k,
                            emptyEditAlignment.operations,
                            thisValue,
                            empty,
                            emptyEditAlignment.cost,
                        ),
                        KeyValueAlignmentOperation.Delete(
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
                        KeyValueAlignmentOperation.Edit(
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

    override fun empty(): AlignableKeyValue<K, E, O> {
        return AlignableKeyValue(immutableLinkedMapOf())
    }

    override fun applyAlignment(alignmentOperations: List<KeyValueAlignmentOperation<K, E, O>>): AlignableKeyValue<K, E, O> {
        val compoundListener = KeyValueAlignmentListener.compoundListenerOf(listeners)
        compoundListener.didEnterMap(this)
        val elementMap = LinkedHashMap<K, E>().also { lhm ->
            lhm.putAll(elements)
        }
        for ((i, op) in alignmentOperations.withIndex()) {
            val key = op.atKey
            when (op) {
                is KeyValueAlignmentOperation.Delete -> {
                    elementMap.remove(key)
                    compoundListener.didRemoveElement(i, op.atKey, op.element)
                }

                is KeyValueAlignmentOperation.Edit -> elementMap.compute(key) { _, e ->
                    // Could technically just take the final element here, but replaying enables listeners and such
                    (e?.applyAlignment(op.alignment) ?: op.element)
                        .also {
                            compoundListener.didEditElement(i, op.atKey, op.alignment, op.fromElement, op.element)
                        }
                }

                is KeyValueAlignmentOperation.Insert -> {
                    val targetElement = op.element
                    elementMap[key] = targetElement
                    compoundListener.didInsertElement(i, op.atKey, op.element)
                }
            }
        }

        return AlignableKeyValue(ImmutableLinkedMap.ofLinkedHashMap(elementMap))
            .also { compoundListener.didExitMap(it) }
    }

    override fun toString(): String {
        return yamlMapper.writeValueAsString(elements)
    }

    companion object {
        private val yamlMapper = Mappers.yamlMapper
    }
}