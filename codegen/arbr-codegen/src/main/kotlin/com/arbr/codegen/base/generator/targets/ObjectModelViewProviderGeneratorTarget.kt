package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator

internal data object ObjectModelViewProviderGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_view_provider.kt.mustache"),
)
