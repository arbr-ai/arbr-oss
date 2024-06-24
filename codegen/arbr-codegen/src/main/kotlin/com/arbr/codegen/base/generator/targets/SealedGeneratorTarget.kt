package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetFileSpecifier
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetReplicationStrategy

internal sealed class SealedGeneratorTarget(
    replicationStrategy: GeneratorTargetReplicationStrategy,
    fileSpecifier: GeneratorTargetFileSpecifier,
    mapper: GeneratorTargetModelMapper,
    generator: GeneratorTargetOutputGenerator,
): GeneratorTarget(replicationStrategy, fileSpecifier, mapper, generator) {

    /**
     * Default = per file replicator and KtSource specifier
     */
    constructor(
        mapper: GeneratorTargetModelMapper,
        generator: GeneratorTargetOutputGenerator
    ): this(
        GeneratorTargetReplicationStrategy.PerTable,
        GeneratorTargetFileSpecifier.KtSourcePackageDerived,
        mapper,
        generator
    )

    /**
     * Default = per file replicator and KtSource specifier
     */
    constructor(
        generator: GeneratorTargetOutputGenerator
    ): this(
        GeneratorTargetModelMapper.Id,
        generator
    )
}
