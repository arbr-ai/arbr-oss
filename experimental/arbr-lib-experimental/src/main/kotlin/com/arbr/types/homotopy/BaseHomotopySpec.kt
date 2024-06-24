package com.arbr.types.homotopy

import com.arbr.types.homotopy.spec.HomotopySpec

object BaseHomotopySpec : HomotopySpec<PlainType> {

    override fun liftNode(
        context: MutablePathContext,
        valueTypeImplementor: PlainType,
        innerImplementors: List<PlainType>
    ): PlainType {
        return valueTypeImplementor // ?
    }
}
