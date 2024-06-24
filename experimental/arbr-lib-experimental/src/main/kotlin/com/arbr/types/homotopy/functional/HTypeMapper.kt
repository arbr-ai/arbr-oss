package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.MutablePathContext
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.htype.NodeHType
import com.arbr.types.homotopy.htype.PrimitiveHType
import com.arbr.types.homotopy.spec.HomotopySpec

interface HTypeMapper<Tr, F : Tr, Tr2, G : Tr2> : Visitor<Tr, Tr2> {
    val spec: HomotopySpec<Tr2>

    fun mapBaseTypeWithContext(f: F, context: MutablePathContext): G

    private val hTypeReducerFunction: HTypeReducerFunction<Tr, F, HType<Tr2, G>>
        get() = HTypeReducerFunction { propertyKey, hType, intermediateProduct, context ->
            when (hType) {

                is PrimitiveHType -> {
                    spec.constructPrimitive(
                        (intermediateProduct.tList[0] as PrimitiveHType<Tr2, G>).baseTypeRepresentation,
                        context,
                    )
                }

                is NodeHType -> {
                    val first = intermediateProduct.tList.first() as PrimitiveHType<Tr2, G>
                    val propertyKeys = hType.childHTypes.map { it.first }
                    spec.constructNode(
                        first.baseTypeRepresentation,
                        propertyKeys.zip(intermediateProduct.tList.drop(1)).map { (pKey, childType) ->
                            pKey to childType as NodeHType<Tr2, G>
                        },
                        context
                    )
                }
            }
        }

    private val primitiveFn: HPrimitiveContextualFunction<Tr, F, HType<Tr2, G>>
        get() = HPrimitiveContextualFunction { f, innerContext ->
            val mapped = mapBaseTypeWithContext(f, innerContext)
            spec.constructPrimitive(
                mapped,
                innerContext,
            )
        }

    fun mapWithContext(hType: HType<Tr, F>, context: MutablePathContext): HType<Tr2, G> {
        return HTypeMapUtils.mapWithContext(
            context,
            primitiveFn,
            hTypeReducerFunction,
            hType,
            preOrderVisitor,
        ) {
            postOrderVisitor.ingest(it.implementor)
        }
    }
}
