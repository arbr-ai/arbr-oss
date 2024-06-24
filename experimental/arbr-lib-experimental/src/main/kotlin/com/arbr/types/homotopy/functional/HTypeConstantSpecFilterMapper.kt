package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.HomotopySpecBijectionWrapper
import com.arbr.types.homotopy.MutablePathContext
import com.arbr.types.homotopy.NullableTrait
import com.arbr.types.homotopy.NullableTraitImplementor
import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.spec.HomotopySpec

/**
 * Map from implementors of one trait to another while filtering undesirables
 * Essentially `mapNotNull`
 */
class HTypeConstantSpecFilterMapper<Tr, F : Tr, Tr2 : Any>(
    private val targetSpec: HomotopyFilterSpec<Tr2>,
    private val mapSpec: HomotopyGroundMap<Tr, F, Tr2?, Tr2?>,
) : HTypeFilterMapper<Tr, F, Tr2> {
    override val spec: HomotopySpec<NullableTrait<Tr2>> = HomotopySpecBijectionWrapper(
        targetSpec,
        { NullableTraitImplementor(it) },
        { it.traitOrNull },
    )

    override fun mapBaseTypeWithContext(f: F, context: MutablePathContext): NullableTraitImplementor<Tr2, Tr2> {
        return NullableTraitImplementor(mapSpec.transform(f, context))
    }

    override val postOrderVisitor: Ingestor<NullableTrait<Tr2>> = Ingestor {}

    override val preOrderVisitor: Ingestor<Tr> = Ingestor {}
}