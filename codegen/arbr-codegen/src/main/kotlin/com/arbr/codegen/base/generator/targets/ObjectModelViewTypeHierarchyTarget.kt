package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetFileSpecifier
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetReplicationStrategy

internal data object ObjectModelViewTypeHierarchyTarget : SealedGeneratorTarget(
    GeneratorTargetReplicationStrategy.SingleRoot,
    GeneratorTargetFileSpecifier.KtSourcePackageDerived,
    GeneratorTargetModelMapper.Id,
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_type_hierarchy.kt.mustache"),
)

internal data object ObjectModelViewKvStoreTarget : SealedGeneratorTarget(
    GeneratorTargetReplicationStrategy.SingleRoot,
    GeneratorTargetFileSpecifier.KtSourcePackageDerived,
    GeneratorTargetModelMapper.Id,
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_kv_store.kt.mustache"),
)


internal data object ObjectModelViewForeignKeyTarget : SealedGeneratorTarget(
    GeneratorTargetReplicationStrategy.SingleRoot,
    GeneratorTargetFileSpecifier.KtSourcePackageDerived,
    GeneratorTargetModelMapper.Id,
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_foreign_key.kt.mustache"),
)

