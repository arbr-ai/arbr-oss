package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetFileSpecifier
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetReplicationStrategy

internal data object ResourceTypesGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetReplicationStrategy.SingleRoot,
    GeneratorTargetFileSpecifier.KtSourcePackageDerived,
    GeneratorTargetModelMapper.Id,
    GeneratorTargetOutputGenerator.ArbrTemplate("ResourceTypes.kttmpl"),
)
