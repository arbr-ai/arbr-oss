package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyIntrospectionConfig
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.htype.NodeHType
import com.arbr.types.homotopy.keys.KeyPathTokenKind
import org.slf4j.LoggerFactory
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

/**
 * Introspection functionality for the base representation of the type graph
 */
@Suppress("SameParameterValue")
class HomotopyIntrospection(
    private val config: HomotopyIntrospectionConfig,
) {
    private val plainTypeIntrospection = PlainTypeIntrospection(config)

    private fun getNodeHType(
        plainType: PlainType,
        context: MutablePathContext,
        nodeMap: Map<PlainType.Ref, OrderedPlainTypeNode>,
        stack: MutableList<PlainType.Ref>,
    ): NodeHType<PlainType, PlainType> {
        val token = when (plainType) {
            is PlainType.Leaf -> KeyPathTokenKind.Primitive(plainType.kType.jvmErasure.simpleName ?: "*")
            is PlainType.ListOf -> KeyPathTokenKind.List
            is PlainType.MapOf -> KeyPathTokenKind.Map
            is PlainType.NullableOf -> KeyPathTokenKind.Nullable
            is PlainType.Ref -> KeyPathTokenKind.ResourceBody(plainType.kType.jvmErasure.simpleName ?: "*")
        }

        return context.with(token) {
            val children = context.with(KeyPathTokenKind.NodeChildren) {
                when (plainType) {
                    is PlainType.Leaf -> emptyList()
                    is PlainType.ListOf -> {
                        val key = "element"
                        listOf(
                            context.with(KeyPathTokenKind.PropertyElement(key, 0)) {
                                key to getNodeHType(plainType.inner, context, nodeMap, stack)
                            }
                        )
                    }
                    is PlainType.MapOf -> {
                        val key = "key"
                        val valueKey = "value"
                        listOf(
                            context.with(KeyPathTokenKind.PropertyElement(key, 0)) {
                                key to getNodeHType(plainType.innerKey, context, nodeMap, stack)
                            },
                            context.with(KeyPathTokenKind.PropertyElement(valueKey, 1)) {
                                valueKey to getNodeHType(plainType.innerValue, context, nodeMap, stack)
                            },
                        )
                    }
                    is PlainType.NullableOf -> listOf(
                        context.with(KeyPathTokenKind.Nullable) {
                            "value" to getNodeHType(plainType.inner, context, nodeMap, stack)
                        }
                    )
                    is PlainType.Ref -> {
                        if (plainType in stack) {
                            emptyList()
                        } else {
                            stack.add(plainType)
                            nodeMap[plainType]!!.node.children.mapIndexed { i, (pKey, childType) ->
                                context.with(KeyPathTokenKind.PropertyElement(pKey.name, i)) {
                                    pKey.name to getNodeHType(childType, context, nodeMap, stack)
                                }
                            }
                        }
                    }
                }
            }

            config.baseHomotopySpec.constructNode<PlainType>(
                plainType,
                children,
                context,
            )
        }
    }

    fun introspectBaseType(objectType: KType, context: MutablePathContext): HType<PlainType, PlainType> {
        val plainTypeNodes = plainTypeIntrospection.introspectBaseType(objectType)
        val plainTypeNodeMap = plainTypeNodes.associateBy { it.node.ref }
        val requestedNode = plainTypeNodes.firstOrNull {
            it.node.ref.kType == objectType
        } ?: throw Exception("Base type not in introspected nodes")

        val stack = mutableListOf<PlainType.Ref>()
        val mappedNode = getNodeHType(requestedNode.node.ref, context, plainTypeNodeMap, stack)

        return mappedNode
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HomotopyIntrospection::class.java)
    }
}