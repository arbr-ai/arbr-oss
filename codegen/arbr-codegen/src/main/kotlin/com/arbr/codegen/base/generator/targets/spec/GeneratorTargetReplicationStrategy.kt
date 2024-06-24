package com.arbr.codegen.base.generator.targets.spec

import com.arbr.codegen.base.generator.DisplayRootModel
import java.util.stream.Stream

internal sealed interface GeneratorTargetReplicationStrategy {

    fun replicate(displayRootModel: DisplayRootModel): Stream<DisplayRootModel>

    data object SingleRoot : GeneratorTargetReplicationStrategy {
        override fun replicate(displayRootModel: DisplayRootModel): Stream<DisplayRootModel> {
            return listOf(displayRootModel).stream()
        }
    }

    data object PerTable : GeneratorTargetReplicationStrategy {
        override fun replicate(displayRootModel: DisplayRootModel): Stream<DisplayRootModel> {
            return displayRootModel.schema.stream().flatMap { schema ->
                schema.table.stream().map { table ->
                    displayRootModel.copy(schema = listOf(schema.copy(table = listOf(table))))
                }
            }
        }
    }

    data object PerField : GeneratorTargetReplicationStrategy {
        override fun replicate(displayRootModel: DisplayRootModel): Stream<DisplayRootModel> {
            return displayRootModel.schema.stream().flatMap { schema ->
                schema.table.stream().flatMap { table ->
                    table.field.stream().map { field ->
                        val newTable = table.copy(field = listOf(field))
                        displayRootModel.copy(schema = listOf(schema.copy(table = listOf(newTable))))
                    }
                }
            }
        }
    }
}