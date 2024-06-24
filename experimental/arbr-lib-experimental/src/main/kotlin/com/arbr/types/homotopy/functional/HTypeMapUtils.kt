package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.MutablePathContext
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.htype.NodeHType
import com.arbr.types.homotopy.htype.PrimitiveHType


sealed class HTypeMapIntermediateProduct<Tr, F : Tr, T> {
    abstract val hTypeClass: Class<out HType<Tr, F>>

    abstract val tList: List<T>

    /**
     * One type parameter
     */
    data class One<Tr, F : Tr, T>(
        override val hTypeClass: Class<out HType<Tr, F>>,
        val t0: T,
    ) : HTypeMapIntermediateProduct<Tr, F, T>() {
        override val tList: List<T> = listOf(t0)
    }

    data class Many<Tr, F : Tr, T>(
        override val hTypeClass: Class<out HType<Tr, F>>,
        override val tList: List<T>,
    ) : HTypeMapIntermediateProduct<Tr, F, T>()
}

fun interface HTypeFunction<Tr, F : Tr, T> {
    operator fun invoke(hType: HType<Tr, F>): T
}

fun interface HPrimitiveContextualFunction<Tr, F : Tr, T> {
    operator fun invoke(primitiveValue: F, context: MutablePathContext): T
}

fun interface HTypeReducerFunction<Tr, F : Tr, T> {
    operator fun invoke(
        propertyKey: String,
        hType: HType<Tr, F>,
        intermediateProduct: HTypeMapIntermediateProduct<Tr, F, T>,
        context: MutablePathContext
    ): T
}


object HTypeMapUtils {

    private class MappingHelper<Tr, F : Tr, T>(
        private val primitiveFn: HPrimitiveContextualFunction<Tr, F, T>,
        private val reducerFn: HTypeReducerFunction<Tr, F, T>,
        private val preOrderVisitor: Ingestor<Tr> = Ingestor { },
        private val postOrderVisitor: Ingestor<T> = Ingestor { },
    ) {
        private data class Box<Q>(val value: Q)
        private val parentRefMap = mutableMapOf<PrimitiveHType<Tr, F>, Box<T>>()

        fun mapRec(propertyKey: String, hType: HType<Tr, F>, context: MutablePathContext): T {
            preOrderVisitor.ingest(hType.implementor)
            val nTokens = context.pathTokens.size

            val oldPath = context.pathTokens.toList()
            context.pathTokens.clear()
            context.pathTokens.addAll(hType.keyPath.pathTokens)

            val intermediateProduct: HTypeMapIntermediateProduct<Tr, F, T> = when (hType) {
                is PrimitiveHType -> {
                    val t0 = primitiveFn(hType.baseTypeRepresentation, context)
                    HTypeMapIntermediateProduct.One(hType::class.java, t0)
                }

                is NodeHType -> {
                    val t0 = primitiveFn(hType.baseValue, context)
                    val tList = hType.childHTypes.map { (propertyKey, hType) ->
                        mapRec(propertyKey, hType, context)
                    }
                    HTypeMapIntermediateProduct.Many(hType::class.java, listOf(t0) + tList)
                }
            }

            val result = reducerFn(propertyKey, hType, intermediateProduct, context)

            context.pathTokens.clear()
            context.pathTokens.addAll(oldPath)

            check(context.pathTokens.size == nTokens) {
                "Started with $nTokens, ended with ${context.pathTokens.size} - Class: ${hType::class.java}"
            }

            return result
                .also {
                    if (hType is PrimitiveHType) {
                        parentRefMap[hType] = Box(it)
                    }
                }
                .also(postOrderVisitor::ingest)
        }
    }

    private fun <Tr, F : Tr, T> mappingWithContext(
        context: MutablePathContext,
        primitiveFn: HPrimitiveContextualFunction<Tr, F, T>,
        reducerFn: HTypeReducerFunction<Tr, F, T>,
        preOrderVisitor: Ingestor<Tr> = Ingestor { },
        postOrderVisitor: Ingestor<T> = Ingestor { },
    ): HTypeFunction<Tr, F, T> {
        val helper = MappingHelper(
            primitiveFn,
            reducerFn,
            preOrderVisitor,
            postOrderVisitor,
        )
        return HTypeFunction { hType ->
            helper.mapRec("", hType, context)
        }
    }

    fun <Tr, F : Tr, T> mapWithContext(
        context: MutablePathContext,
        primitiveFn: HPrimitiveContextualFunction<Tr, F, T>,
        reducerFn: HTypeReducerFunction<Tr, F, T>,
        hType: HType<Tr, F>,
        preOrderVisitor: Ingestor<Tr> = Ingestor { },
        postOrderVisitor: Ingestor<T> = Ingestor { },
    ): T {
        val mapping = mappingWithContext(context, primitiveFn, reducerFn, preOrderVisitor, postOrderVisitor)

        return mapping.invoke(hType)
    }
}
