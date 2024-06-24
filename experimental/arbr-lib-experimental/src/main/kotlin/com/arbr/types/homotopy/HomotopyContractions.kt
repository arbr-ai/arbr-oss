package com.arbr.types.homotopy

import com.arbr.types.homotopy.functional.HPrimitiveContextualFunction
import com.arbr.types.homotopy.functional.HTypeMapUtils
import com.arbr.types.homotopy.functional.HTypeReducerFunction
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.spec.ContractionHomotopy

class HomotopyContractions {

    private fun <Tr, F : Tr> hTypeReducerFunction(
        contractibleSpec: ContractionHomotopy<Tr>
    ): HTypeReducerFunction<NullableTrait<Tr>, NullableTraitImplementor<Tr, F>, List<LocalPathQualifiedNode<Tr>>> = HTypeReducerFunction { propertyKey, hType, intermediateProduct, context ->
        val childElements = intermediateProduct.tList.flatten()

        val thisTraitValue = hType.implementor.traitOrNull
        if (thisTraitValue == null) {
            // Pass the set of viable paths along
            childElements.map { localPathQualifiedNode ->
                // Prepend
                localPathQualifiedNode.copy(
                    localPathTokens = listOf(propertyKey) + localPathQualifiedNode.localPathTokens,
                )
            }
        } else {
            // Take the subpaths as our own node's new out-relations, and dutifully pass along this node as the next
            // tail
            // We lose some structure here necessarily, but it makes more sense to use compound keys to represent
            // contracted relations than to try to retain the compositional structure
            val newNodeType = contractibleSpec.constructNode(
                thisTraitValue,
                childElements.map { localPathQualifiedNode ->
                    val combinedKey = localPathQualifiedNode.localPathTokens.joinToString(".")
                    combinedKey to localPathQualifiedNode.nodeHType
                },
                context
            )

            // Top-down path tokens -> global key
            listOf(
                LocalPathQualifiedNode(
                    listOf(propertyKey),
                    newNodeType,
                )
            )
        }
    }

    private fun <Tr> contractInner(
        contractibleSpec: ContractionHomotopy<Tr>,
        nullableHType: HType<NullableTrait<Tr>, NullableTraitImplementor<Tr, Tr>>,
        context: MutablePathContext,
    ): List<LocalPathQualifiedNode<Tr>> {
        val reducerFn = hTypeReducerFunction<Tr, Tr>(contractibleSpec)
        val primitiveFn = HPrimitiveContextualFunction<NullableTrait<Tr>, NullableTraitImplementor<Tr, Tr>, List<LocalPathQualifiedNode<Tr>>> { _, _ ->
            emptyList()
        }

        return HTypeMapUtils.mapWithContext(
            context,
            primitiveFn,
            reducerFn,
            nullableHType,
        )
    }

    /**
     * Contract the type to the "non-null" traited types, with property keys representing paths to their roots
     *
     * We can't quite get the return type to be HType<Tr, F> for a good reason - since we're contracting, we might end
     * up with primitive nodes derived from their traits rather than concrete values
     */
    fun <Tr> contract(
        baseHomotopySpec: ContractionHomotopy<Tr>,
        nullableHType: HType<NullableTrait<Tr>, NullableTraitImplementor<Tr, Tr>>,
    ): List<LocalPathQualifiedNode<Tr>> {
        val context = MutablePathContext.new()
        val topLevelNontrivialTraitedTypes = contractInner(
            baseHomotopySpec,
            nullableHType,
            context,
        )

        return topLevelNontrivialTraitedTypes
    }
}
