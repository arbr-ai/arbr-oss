package com.arbr.codegen.base.generator.targets

data class GeneratorTargetOutput(
    val outputCollection: GeneratorTargetOutputCollection,
    val subPath: String,
    val content: String,
)