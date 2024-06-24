package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.MutablePathContext
import com.arbr.types.homotopy.spec.HomotopySpec

class HTypeConstantSpecMapper<Tr, F : Tr, Tr2, G : Tr2>(
    override val spec: HomotopySpec<Tr2>,
    val transform: (F, MutablePathContext) -> G,
    private val visitPreOrder: Ingestor<Tr> = Ingestor { },
    private val visitPostOrder: Ingestor<Tr2> = Ingestor { },
) : HTypeMapper<Tr, F, Tr2, G>,
    Visitor<Tr, Tr2> by VisitorImpl(visitPreOrder, visitPostOrder) {

    override fun mapBaseTypeWithContext(f: F, context: MutablePathContext): G {
        return transform(f, context)
    }
}
