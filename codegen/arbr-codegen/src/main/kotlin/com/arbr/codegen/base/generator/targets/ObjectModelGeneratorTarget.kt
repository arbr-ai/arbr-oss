package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator

internal data object ObjectModelGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetOutputGenerator.ArbrTemplate("ObjectModelResource.kttmpl")
)
