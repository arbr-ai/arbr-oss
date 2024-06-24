package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator

internal data object PartialTypeTensorsGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetOutputGenerator.ArbrTemplate("PartialTypeTensor.kttmpl")
)
