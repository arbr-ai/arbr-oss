package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.htype.HType

interface HTypeReducer<Tr, F : Tr, V> {
    val reducer: Reducer<Tr, V>

    fun reduce(
        hType: HType<Tr, F>,
    ): V {
        val visitor: HTypeVisitorMapper<Tr, F> = HTypeVisitorMapper(
            reducer.preOrderVisitor,
            reducer.postOrderVisitor,
        )

        visitor.walk(
            hType,
        )
        return reducer.yield()
    }
}
