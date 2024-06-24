package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator

internal data object ObjectModelViewGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_view.kt.mustache"),
)

internal data object ObjectModelPlainGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_plain.kt.mustache"),
)
