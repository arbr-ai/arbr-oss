package com.arbr.types.homotopy

import com.arbr.object_model.core.model.ArbrRootModel
import com.arbr.types.homotopy.keys.KeyPathTokenKind
import com.arbr.types.homotopy.util.HTypeIndentingStringReducer
import org.junit.jupiter.api.Test
import kotlin.reflect.full.starProjectedType

sealed class DetachedItemIdentifier {
    data class Resource(val name: String): DetachedItemIdentifier()
    data class Property(val name: String, val typeName: String): DetachedItemIdentifier()
}

class PropertyIdentifierHomotopyTest {

    private fun itemIdToken(pathToken: KeyPathTokenKind): String? {
        when (pathToken) {
            KeyPathTokenKind.List -> TODO()
            KeyPathTokenKind.Map -> TODO()
            KeyPathTokenKind.NodeChildren -> TODO()
            KeyPathTokenKind.NodeValue -> TODO()
            KeyPathTokenKind.Nullable -> TODO()
            is KeyPathTokenKind.Primitive -> TODO()
            is KeyPathTokenKind.PropertyElement -> TODO()
            is KeyPathTokenKind.ResourceBody -> TODO()
        }
    }

    @Test
    fun `synthesizes identifiers by homotopy`() {
        val baseType = Homotopy.getBaseType<ArbrRootModel>()

        val allIdentifiers = baseType
            .contraction()
            .mapPrimitivesNotNull { plainType, pathContext ->
//                when (plainType) {
//                    is PlainType.Leaf -> DetachedItemIdentifier.Property(pathContext.pathTokens.filterIsInstance<KeyPathTokenKind.PropertyElement>().last().key, plainType.kType.jvmErasure.simpleName ?: "*")
//                    is PlainType.ListOf -> {
//                        null
//                    }
//                    is PlainType.MapOf -> null
//                    is PlainType.NullableOf -> null
//                    is PlainType.Ref -> DetachedItemIdentifier.Resource(plainType.kType.jvmErasure.simpleName ?: "*")
//                }
                if (plainType is PlainType.Ref) {
                    plainType to emptyList<PlainType>()
                } else if (plainType is PlainType.Leaf && plainType.kType == Long::class.starProjectedType) {
                    plainType to emptyList<PlainType>()
                } else {
                    null
                }
            }
            .liftNodes { _, nodeValue, childValues ->
//                if (nodeValue != null && nodeValue is DetachedItemIdentifier.Resource) {
//                    nodeValue
//                } else {
//                    null
//                }
                if (nodeValue == null) {
                    null
                } else {
                    val (node, imch) = nodeValue
                    node to (imch + childValues.mapNotNull { it?.first })
                }
            }()

        allIdentifiers.forEach {
            println(it.localPathTokens.joinToString(".") + "  " + it.nodeHType.baseValue)
            println(
                HTypeIndentingStringReducer<Pair<PlainType, List<PlainType>>>(
                { (node, imch), _ ->
                    appendLine("$node  $imch")
                },
                {  (node, imch), _ ->
                    appendLine("/$node")
                },
            ).reduce(it.nodeHType))
            println("===")
        }

    }

}