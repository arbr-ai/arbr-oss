package com.arbr.platform.object_graph.alignable

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.platform.object_graph.impl.ObjectModelResource

class PartialTrackingAlignableMap<ForeignKey: NamedForeignKey>(
    private val nodeMap: ImmutableLinkedMap<String, AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>>,
) : AlignableDependentMap<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>(
    nodeMap
) {

    override fun unsatisfiedDependenciesDisplayStrings(
        currentElementMap: Map<String, AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>>,
        alignmentOperations: List<MapAlignmentOperation<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>>,
        completedOperations: List<Pair<Int, MapAlignmentOperation<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>>>,
        operation: MapAlignmentOperation<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>
    ): List<String> {
        val unsat = mutableListOf<String>()

        // Edit depends on any inserts that may exist in the op list
        // Delete depends on any edits and inserts that may exist in the op list
        val indexDeps: List<Int> = when (operation) {
            is MapAlignmentOperation.Delete -> {
                alignmentOperations.withIndex().filter { (_, op2) ->
                    when (op2) {
                        is MapAlignmentOperation.Delete -> {
                            op2.element.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element } // Node is parent of a child to be deleted; delete child first
                        }

                        is MapAlignmentOperation.Edit -> {
                            op2.element.alignableElement.uuid == operation.element.alignableElement.uuid // Node is edited later
                                    || op2.element.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element } // Node is parent of a child to be edited
                                    || op2.fromElement.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element } // Node is parent of a child to be edited
                        }

                        is MapAlignmentOperation.Insert -> {
                            op2.element.alignableElement.uuid == operation.element.alignableElement.uuid // Node is inserted later
                                    || op2.element.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element } // Node is parent of a child to be inserted
                        }
                    }
                }.map { it.index }
            }

            is MapAlignmentOperation.Edit -> {
                alignmentOperations.withIndex().mapNotNull { (j, op2) ->
                    if (op2.element.alignableElement.uuid == operation.element.alignableElement.uuid) {
                        when (op2) {
                            is MapAlignmentOperation.Delete -> null
                            is MapAlignmentOperation.Edit -> null
                            is MapAlignmentOperation.Insert -> j
                        }
                    } else {
                        null
                    }
                }
            }

            is MapAlignmentOperation.Insert -> emptyList()
        }

        val completedIndices = completedOperations.map { it.first }.toSet()
        val unsatisfiedIndices = indexDeps.filter { it !in completedIndices }
        for (unsatisfiedIndex in unsatisfiedIndices) {
            unsat.add("Operation $unsatisfiedIndex")
        }

        val parentUuids = when (operation) {
            is MapAlignmentOperation.Delete -> operation.element.alignableElement.parentUuids.mapNotNull { it.value.uuid }
            is MapAlignmentOperation.Edit -> {
                // XOR
                val oldParents =
                    operation.fromElement.alignableElement.parentUuids.mapNotNull { it.value.uuid }.toSet()
                val newParents =
                    operation.element.alignableElement.parentUuids.mapNotNull { it.value.uuid }.toSet()
                ((newParents - oldParents) + (oldParents - newParents)).toList()
            }

            is MapAlignmentOperation.Insert -> operation.element.alignableElement.parentUuids.mapNotNull { it.value.uuid }
        }

        val unsatisfiedParentUuids = parentUuids.filter { it !in currentElementMap }
        for (unsatisfiedParentUuid in unsatisfiedParentUuids) {
            unsat.add("Parent UUID: $unsatisfiedParentUuid")
        }

        return unsat
    }

    override fun operationIsSatisfied(
        currentElementMap: Map<String, AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>>,
        alignmentOperations: List<MapAlignmentOperation<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>>,
        unsatisfiedIndices: Set<Int>,
        operation: MapAlignmentOperation<AlignableProxy<ObjectModelResource<*, *, ForeignKey>, PartialNodeAlignableValue, PartialOperation>, PartialOperation>
    ): Boolean {
        when (operation) {
            is MapAlignmentOperation.Delete -> {
                for (j in unsatisfiedIndices) {
                    val op2 = alignmentOperations[j]
                    val dependencyCondition = when (op2) {
                        is MapAlignmentOperation.Delete -> {
                            // Node is parent of a child to be deleted; delete child first
                            op2.element.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element }
                        }

                        is MapAlignmentOperation.Edit -> {
                            op2.element.alignableElement.uuid == operation.element.alignableElement.uuid // Node is edited later
                                    || op2.element.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element } // Node is parent of a child to be edited
                                    || op2.fromElement.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element } // Node is parent of a child to be edited
                        }

                        is MapAlignmentOperation.Insert -> {
                            op2.element.alignableElement.uuid == operation.element.alignableElement.uuid // Node is inserted later
                                    || op2.element.alignableElement.parentUuids.any { it.value.uuid == operation.element.alignableElement.uuid.element } // Node is parent of a child to be inserted
                        }
                    }

                    if (dependencyCondition) {
                        // Dependent on an unsatisfied operation
                        return false
                    }
                }
            }

            is MapAlignmentOperation.Edit -> {
                for (j in unsatisfiedIndices) {
                    val op2 = alignmentOperations[j]
                    if (op2.element.alignableElement.uuid == operation.element.alignableElement.uuid && op2 is MapAlignmentOperation.Insert) {
                        return false
                    }
                }
            }

            is MapAlignmentOperation.Insert -> {
                // Pass
            }
        }

        val parentUuids = when (operation) {
            is MapAlignmentOperation.Delete -> operation.element.alignableElement.parentUuids.mapNotNull { it.value.uuid }
            is MapAlignmentOperation.Edit -> {
                // XOR
                val oldParents =
                    operation.fromElement.alignableElement.parentUuids.mapNotNull { it.value.uuid }.toSet()
                val newParents =
                    operation.element.alignableElement.parentUuids.mapNotNull { it.value.uuid }.toSet()
                ((newParents - oldParents) + (oldParents - newParents)).toList()
            }

            is MapAlignmentOperation.Insert -> operation.element.alignableElement.parentUuids.mapNotNull { it.value.uuid }
        }

        return parentUuids.all { it in currentElementMap }
    }
}