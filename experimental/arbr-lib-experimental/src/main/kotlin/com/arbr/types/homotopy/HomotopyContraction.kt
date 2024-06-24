package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.functional.HTypeConstantSpecFilterMapper
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.spec.ContractionHomotopy

abstract class HomotopyContraction<Trait, F: Trait, TargetTrait: Any> {
    protected abstract val hType: HType<Trait, F>
    protected abstract val transform: (Trait, MutablePathContext) -> TargetTrait?
    protected abstract val filterSpec: HomotopyFilterSpec<TargetTrait>
    protected abstract val contractionHomotopy: ContractionHomotopy<TargetTrait>

    operator fun invoke(): List<LocalPathQualifiedNode<TargetTrait>> {
        val mapper: HTypeConstantSpecFilterMapper<Trait, F, TargetTrait> = HTypeConstantSpecFilterMapper(filterSpec, transform)
        return mapper.filterMap(
            contractionHomotopy,
            hType,
            MutablePathContext.new(),
        )
    }
}