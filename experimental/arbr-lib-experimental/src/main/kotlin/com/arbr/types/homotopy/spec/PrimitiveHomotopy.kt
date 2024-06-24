package com.arbr.types.homotopy.spec

import com.arbr.types.homotopy.MutablePathContext
import com.arbr.types.homotopy.htype.PrimitiveHType
import com.arbr.types.homotopy.keys.CompoundKeyPathPropertyKey


interface PrimitiveHomotopy<Tr> {
    fun <F: Tr> constructPrimitive(
        baseElement: F,
        context: MutablePathContext,
    ): PrimitiveHType<Tr, F> {
        return PrimitiveHType(
            CompoundKeyPathPropertyKey(context.pathTokens.toList()),
            baseElement,
        )
    }
}
