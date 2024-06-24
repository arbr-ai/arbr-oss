package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetFileSpecifier
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetReplicationStrategy

internal data object ObjectModelFieldGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetReplicationStrategy.PerField,
    GeneratorTargetFileSpecifier.KtSourcePackageDerived,
    GeneratorTargetModelMapper.Id,
    GeneratorTargetOutputGenerator.ArbrTemplate("ObjectModelResourceFieldValue.kttmpl")
)
