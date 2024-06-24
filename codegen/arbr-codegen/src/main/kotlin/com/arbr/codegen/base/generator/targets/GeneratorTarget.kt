package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.DisplayRootModel
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetFileSpecifier
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetReplicationStrategy
import java.util.stream.Stream

internal open class GeneratorTarget(
    private val replicationStrategy: GeneratorTargetReplicationStrategy,
    private val fileSpecifier: GeneratorTargetFileSpecifier,
    private val mapper: GeneratorTargetModelMapper,
    private val generator: GeneratorTargetOutputGenerator,
) {
    val templateFileName: String? = generator.templateFileName

    fun generate(
        displayRootModel: DisplayRootModel
    ): Stream<GeneratorTargetOutput> {
        return replicationStrategy.replicate(displayRootModel)
            .map(mapper::transform)
            .map { rootModel ->
                val output = generator.generate(rootModel)
                fileSpecifier.specifyOutputLocation(rootModel, output)
            }
    }
}