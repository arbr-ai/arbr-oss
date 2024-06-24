package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.keys.PropertyKey

class HomotopyCoalescingSpec<BaseTrait, TargetTrait : BaseTrait & Any> : HomotopyFilterSpec<TargetTrait> {

    override fun liftNode(
        context: MutablePathContext,
        valueTypeImplementor: TargetTrait?,
        innerImplementors: List<TargetTrait?>
    ): TargetTrait? {
        return valueTypeImplementor ?: innerImplementors.firstNotNullOfOrNull { it }
    }

}