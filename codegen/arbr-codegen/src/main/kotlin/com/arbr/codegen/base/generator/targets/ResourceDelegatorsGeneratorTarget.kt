package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator

internal data object ResourceDelegatorsGeneratorTarget : SealedGeneratorTarget(
    mapper = GeneratorTargetModelMapper { displayRootModel ->
        val schema = displayRootModel.schema.first()
        val table = schema.table.first()

        // Small hack: filter out self-references by looking at fields with same qualified table name
        val qualifiedTypeName = schema.titleName + table.titleName

        val tdm = table.copy(
            foreignRecord = table.foreignRecord.filter { f ->
                f.resourcePropertyQualifiedType != qualifiedTypeName
            },
            foreignTypeRecord = table.foreignTypeRecord.filter { f ->
                !f.foreignRecord.any { it.resourcePropertyQualifiedType == qualifiedTypeName }
            }
        )

        displayRootModel.copy(
            schema = listOf(
                schema.copy(table = listOf(tdm))
            )
        )
    },
    GeneratorTargetOutputGenerator.ArbrTemplate("ResourceDelegator.kttmpl"),
)
