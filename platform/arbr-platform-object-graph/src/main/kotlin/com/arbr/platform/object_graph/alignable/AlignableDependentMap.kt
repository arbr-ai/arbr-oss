package com.arbr.platform.object_graph.alignable

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.alignable.alignable.collections.MapAlignmentListener
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import org.slf4j.LoggerFactory
import java.util.*

abstract class AlignableDependentMap<E : Alignable<E, O>, O>(
    elements: ImmutableLinkedMap<String, E>,
) : AlignableMap<E, O>(elements) {

    abstract fun operationIsSatisfied(
        currentElementMap: Map<String, E>,
        alignmentOperations: List<MapAlignmentOperation<E, O>>,
        unsatisfiedIndices: Set<Int>,
        operation: MapAlignmentOperation<E, O>,
    ): Boolean

    abstract fun unsatisfiedDependenciesDisplayStrings(
        currentElementMap: Map<String, E>,
        alignmentOperations: List<MapAlignmentOperation<E, O>>,
        completedOperations: List<Pair<Int, MapAlignmentOperation<E, O>>>,
        operation: MapAlignmentOperation<E, O>,
    ): List<String>

    override fun applyAlignment(alignmentOperations: List<MapAlignmentOperation<E, O>>): AlignableMap<E, O> {
        val compoundListener = MapAlignmentListener.compoundListenerOf(listeners)
        compoundListener.didEnterMap(this)
        val elementMap = LinkedHashMap<String, E>().also { lhm ->
            lhm.putAll(elements)
        }

        val remainingOperations = TreeMap(alignmentOperations.withIndex().associate { it.index to it.value })
        val completedOperations = TreeMap<Int, MapAlignmentOperation<E, O>>()

        var anyChanged = true
        while (anyChanged) {
            anyChanged = false

            val indicesToRemove = mutableSetOf<Int>()
            for (i in remainingOperations.keys) {
                val op = remainingOperations[i]!!
                if (
                    operationIsSatisfied(
                        elementMap,
                        alignmentOperations,
                        remainingOperations.keys,
                        op,
                    )
                ) {
                    indicesToRemove.add(i)
                    anyChanged = true
                    completedOperations[i] = op

                    // Perform operation
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
                                    compoundListener.didEditElement(
                                        i,
                                        op.atKey,
                                        op.alignment,
                                        op.fromElement,
                                        op.element
                                    )
                                }
                        }

                        is MapAlignmentOperation.Insert -> {
                            val targetElement = op.element
                            elementMap[key] = targetElement
                            compoundListener.didInsertElement(i, op.atKey, op.element)
                        }
                    }
                }
            }

            for (i in indicesToRemove) {
                remainingOperations.remove(i)
            }
        }

        if (remainingOperations.isNotEmpty()) {
            // Try to continue since the alternative is complete failure
            val dependencyUnsatisfiedException =
                DependencyUnsatisfiedException(remainingOperations.flatMap { (index, value) ->
                    unsatisfiedDependenciesDisplayStrings(
                        elementMap,
                        alignmentOperations,
                        completedOperations.toList(),
                        value,
                    ).map {
                        "$index -> $it"
                    }
                })

            logger.error(
                "!!! UNSATISFIED DEPENDENCIES !!!",
                dependencyUnsatisfiedException
            )

            throw dependencyUnsatisfiedException
        }

        return AlignableMap(ImmutableLinkedMap.ofLinkedHashMap(elementMap))
            .also { compoundListener.didExitMap(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AlignableDependentMap::class.java)
    }
}