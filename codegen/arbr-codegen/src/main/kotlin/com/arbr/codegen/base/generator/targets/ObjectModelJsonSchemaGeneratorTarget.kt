package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator

internal data object ObjectModelJsonSchemaGeneratorTarget : SealedGeneratorTarget(
    GeneratorTargetOutputGenerator.MustacheTemplate("object_model_json_schema.kt.mustache"),
)
