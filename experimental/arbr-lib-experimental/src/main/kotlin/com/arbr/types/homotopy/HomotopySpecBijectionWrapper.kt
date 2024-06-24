package com.arbr.types.homotopy

import com.arbr.types.homotopy.spec.HomotopySpec

class HomotopySpecBijectionWrapper<BaseTrait, TargetTrait>(
    private val innerHomotopySpec: HomotopySpec<BaseTrait>,
    private val transformTo: (BaseTrait) -> TargetTrait,
    private val transformFrom: (TargetTrait) -> BaseTrait,
) : HomotopySpec<TargetTrait> {

    override fun liftNode(
        context: MutablePathContext,
        valueTypeImplementor: TargetTrait,
        innerImplementors: List<TargetTrait>
    ): TargetTrait {
        return transformTo(
            innerHomotopySpec.liftNode(
                context,
                transformFrom(valueTypeImplementor),
                innerImplementors.map(transformFrom),
            )
        )
    }
}
