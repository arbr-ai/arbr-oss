package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.htype.NodeHType
import com.arbr.types.homotopy.htype.PrimitiveHType

class HTypeVisitorMapper<Tr, F : Tr>(
    visitPreOrder: Ingestor<Tr>,
    visitPostOrder: Ingestor<Tr>,
) : Visitor<Tr, Tr> by VisitorImpl(visitPreOrder, visitPostOrder) {

    fun walk(hType: HType<Tr, F>) {
        when (hType) {
            is PrimitiveHType -> {
                // Don't ingest for primitive, cheating a bit knowing that the node uses the primitive implementor
            }
            is NodeHType -> {
                preOrderVisitor.ingest(hType.implementor)
                hType.childHTypes.forEach { h ->
                    walk(h.second)
                }
                postOrderVisitor.ingest(hType.implementor)
            }
        }
    }
}
