package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetFileSpecifier
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetReplicationStrategy

internal data object ObjectModelStreamProvidersHierarchyTarget : SealedGeneratorTarget(
    GeneratorTargetReplicationStrategy.SingleRoot,
    GeneratorTargetFileSpecifier.KtSourcePackageDerived,
    GeneratorTargetModelMapper.Id,
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_stream_providers.kt.mustache"),
)
