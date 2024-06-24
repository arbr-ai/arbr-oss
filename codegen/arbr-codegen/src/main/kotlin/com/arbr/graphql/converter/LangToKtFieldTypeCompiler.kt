package com.arbr.graphql.converter

import com.arbr.graphql.converter.expressions.*
import com.arbr.graphql.converter.model.MappedNodeClassModel
import com.arbr.graphql.converter.model.MappedNodeFieldModel
import com.arbr.graphql.converter.model.NodeBaseField
import com.arbr.graphql.converter.util.topSortWith
import java.util.*
import kotlin.reflect.full.starProjectedType

data class LangToKtTypeMapping(
    val baseClassCanonicalName: String,
    val mappedClassSimpleName: String,
)

internal class LangToKtFieldTypeCompiler(
    private val mappedClassModels: List<MappedNodeClassModel>,
    private val providedTypeNodes: Map<String, CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>> = emptyMap(),
    private val providedImplTypeNodes: Map<String, CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>> = emptyMap(),
) {

    private val typeMappingCache: MutableMap<String, Optional<CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>>> =
        mutableMapOf<
                String,
                Optional<CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>>
                >()

    init {
        // Hardcoded type mappings
        kotlin.run {
            val operationType = "graphql.language.OperationDefinition\$Operation"
            val operationNode = CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>(
                DType.GqlSchema.GqlType.Type,
                Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                    KtExpr.Value { target, ctx ->
                        target.append(
                            "com.arbr.graphql.lang.common.GraphQlOperationValue.fromLangValue(${
                                gqlExprValue.render(
                                    ctx
                                )
                            })"
                        )
                    }
                }
            )
            typeMappingCache[operationType] = Optional.of(operationNode)
        }

        kotlin.run {
            val baseTypeName1 = "java.math.BigDecimal"
            val node1 = CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>(
                DType.GqlSchema.GqlType.Type,
                Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                    KtExpr.Value { target, ctx ->
                        gqlExprValue.renderInto(target, ctx)
                        target.append(".toDouble()")
                    }
                }
            )
            typeMappingCache[baseTypeName1] = Optional.of(node1)
        }

        kotlin.run {
            val baseTypeName1 = "java.math.BigInteger"
            val node1 = CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>(
                DType.GqlSchema.GqlType.Type,
                Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                    KtExpr.Value { target, ctx ->
                        gqlExprValue.renderInto(target, ctx)
                        target.append(".toInt()")
                    }
                }
            )
            typeMappingCache[baseTypeName1] = Optional.of(node1)
        }
    }

    private fun getFieldValueTransformerOverTypes(
        gqlType: GqlExpr.LiteralType,
    ): CNode<DType.GqlSchema.GqlType.Field, Expr.Fn<GqlExpr.Value, KtExpr.Value>> {
        return when (gqlType) {
            is GqlExpr.LiteralType.AtomicType -> {
                when (gqlType.literalType) {

                    in providedTypeNodes -> {
                        val cachedNode = providedTypeNodes[gqlType.literalType]!!
                        CNode.of(
                            DType.GqlSchema.GqlType.Field,
                            cachedNode.expr,
                        )
                    }

                    in typeMappingCache -> {
                        val cachedNode = typeMappingCache[gqlType.literalType]!!
                        if (!cachedNode.isPresent) {
                            throw Exception("Dependency cycle")
                        }
                        CNode.of(
                            DType.GqlSchema.GqlType.Field,
                            cachedNode.get().expr,
                        )
                    }

                    in primitiveTypeMapping -> {
                        CNode.of(
                            DType.GqlSchema.GqlType.Field,
                            Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                                // No conversion
                                KtExpr.Value { target, ctx ->
                                    gqlExprValue.renderInto(target, ctx)
                                }
                            },
                        )
                    }

                    in dataClassTypeMapping -> {
                        val mappedModel = mappedClassModels.firstOrNull {
                            it.originClass.canonicalName == gqlType.literalType
                        } ?: throw Exception("Unmappable class type ${gqlType.literalType}")

                        val transformer = getTypeAlignmentTransformer(mappedModel)

                        CNode.of(
                            DType.GqlSchema.GqlType.Field,
                            transformer.expr,
                        )
                    }

                    else -> {
                        throw Exception("Unmappable type ${gqlType.literalType}")
                    }
                }
            }

            is GqlExpr.LiteralType.NullableType<*> -> {
                CNode.of(
                    DType.GqlSchema.GqlType.Field,
                    Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                        val innerTransformer = getFieldValueTransformerOverTypes(gqlType.innerType)
                        KtExpr.Value { target, ctx ->
                            val blockVarName = "p${ctx.depth}"
                            val appliedExpression = innerTransformer.expr.apply(GqlExpr.Value(blockVarName))

                            gqlExprValue.renderInto(target, ctx)
                            target.appendLine()
                            val wrappedTarget = IndentAppendableWrapper(target)
                            wrappedTarget.appendLine("?.let { $blockVarName ->")
                            appliedExpression.renderInto(wrappedTarget, ctx.copy(depth = ctx.depth + 1))
                            wrappedTarget.appendLine()
                            target.append("}")
                        }
                    },
                )
            }

            is GqlExpr.LiteralType.Erased1ParameterType<*> -> {
                getFieldValueTransformerOverTypes(gqlType.outerType)
            }

            is GqlExpr.LiteralType.ListType<*> -> {
                CNode.of(
                    DType.GqlSchema.GqlType.Field,
                    Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                        val innerTransformer = getFieldValueTransformerOverTypes(gqlType.innerType)
                        KtExpr.Value { target, ctx ->
                            val blockVarName = "p${ctx.depth}"
                            val appliedExpression = innerTransformer.expr.apply(GqlExpr.Value(blockVarName))

                            gqlExprValue.renderInto(target, ctx)
                            target.appendLine()
                            val wrappedTarget = IndentAppendableWrapper(target)
                            wrappedTarget.appendLine(".map { $blockVarName ->")
                            appliedExpression.renderInto(wrappedTarget, ctx.copy(depth = ctx.depth + 1))
                            wrappedTarget.appendLine()
                            target.append("}")
                        }
                    },
                )
            }

            is GqlExpr.LiteralType.MapType<*, *> -> {
                CNode.of(
                    DType.GqlSchema.GqlType.Field,
                    Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                        val innerKeyTransformer = getFieldValueTransformerOverTypes(gqlType.innerKeyType)
                        val innerValueTransformer = getFieldValueTransformerOverTypes(gqlType.innerValueType)
                        KtExpr.Value { target, ctx ->
                            val mappedExpression = innerKeyTransformer.expr.apply(GqlExpr.Value("k")).render(ctx)
                            val mappedValueExpression = innerValueTransformer.expr.apply(GqlExpr.Value("v")).render(ctx)

                            if (mappedExpression.trim() == "k" && mappedValueExpression.trim() == "v") {
                                gqlExprValue.renderInto(target, ctx)
                            } else {
                                target.append(
                                    "${gqlExprValue.render(ctx)}.map { (k, v) -> $mappedExpression to $mappedValueExpression }.toMap()"
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    private fun getFieldAlignmentTransformer(
        convert: (GqlExpr.Value) -> KtExpr.Value,
    ): CNode<DType.GqlSchema.GqlType.Field, Expr.Fn<GqlExpr.Value, KtExpr.Value>> {
        return CNode(
            DType.GqlSchema.GqlType.Field,
            Expr.Fn(convert),
        )
    }

    private fun getFieldAlignmentTransformer(
        baseField: NodeBaseField,
        targetField: MappedNodeFieldModel,
    ): CNode<DType.GqlSchema.GqlType.Field, Expr.Fn<GqlExpr.Value, KtExpr.Value>> {
        val baseType = parseGqlTypeString(baseField.fieldValueType.typeName, targetField.nullable)
        val transformer = getFieldValueTransformerOverTypes(baseType)

        // Compose with accessor (messy)
        return CNode<DType.GqlSchema.GqlType.Field, Expr.Fn<GqlExpr.Value, KtExpr.Value>>(
            transformer.dType,
            Expr.Fn {
                transformer.expr.apply(
                    GqlExpr.Value { target, ctx ->
                        it.renderInto(target, ctx)
                        target.append(".")
                        target.append(baseField.accessorLiteral)
                    }
                )
            }
        )
    }

    private fun getTypeAlignmentTransformer(
        convert: (GqlExpr.Value) -> KtExpr.Value,
    ): CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>> {
        return CNode(
            DType.GqlSchema.GqlType.Type,
            Expr.Fn(convert),
        )
    }

    private fun getTypeAlignmentTransformer(
        mappedNodeClassModel: MappedNodeClassModel,
    ): CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>> {
        val baseClass = mappedNodeClassModel.originClass
        val baseClassName = baseClass.canonicalName

        // Memoize but detect infinite loops
        typeMappingCache.compute(baseClassName) { _, optNode ->
            if (optNode == null) {
                // Compute
                Optional.empty()
            } else if (optNode.isPresent) {
                optNode
            } else {
                // Dependency Cycle
                throw Exception("Dependency cycle for type $baseClassName")
            }
        }

        return if (mappedNodeClassModel.objectKind.isInstantiable()) {
            // Align baseFields to targetFields
            val baseFields = baseClass.fields
            val targetFields = mappedNodeClassModel.fieldModels
            check(baseFields.size == baseFields.size) {
                "Field count mismatch for ${mappedNodeClassModel.simpleName}"
            }

            // Requires mapped name represents origin name
            val fieldMapping = baseFields.associate {
                val mappedClassMatch = targetFields.first { mf -> mf.name == it.name }
                it.name to (
                        it to mappedClassMatch
                        )
            }
            check(fieldMapping.size == baseFields.size) {
                "Base node class fields not unique for ${mappedNodeClassModel.simpleName}"
            }

            val fieldTransformerNodes = targetFields.map { mf ->
                val (baseField, _) = fieldMapping[mf.name]!!
                getFieldAlignmentTransformer(baseField, mf)
            }

            val conversion: (GqlExpr.Value) -> KtExpr.Value = { gqlExprValue: GqlExpr.Value ->
                val constructorFnName = mappedNodeClassModel.simpleName
                val mappedFieldExpr = fieldTransformerNodes.map { ftn ->
                    ftn.expr.apply(gqlExprValue)
                }

                KtExpr.Value { target, ctx ->
                    val wrappedTarget = IndentAppendableWrapper(target)
                    target.run {
                        appendLine("$constructorFnName(")
                        mappedFieldExpr.forEach {
                            it.renderInto(wrappedTarget, ctx.copy(depth = ctx.depth + 1))
                            wrappedTarget.appendLine(",")
                        }
                        append(")")
                    }
                }
            }

            getTypeAlignmentTransformer(conversion)
                .also {
                    typeMappingCache[baseClassName] = Optional.of(it)
                }
        } else {
            // Case over subtypes
            // If a proper subtype exists, use as the fallback case, else throw

            /*
            when (node) {
                is graphql.language.EnumTypeDefinition -> {
                    convertEnumTypeDefinition(node)
                }
            }
             */

            fun getMappedModelForBaseClass(baseClassCanonicalName: String): MappedNodeClassModel {
                val candidates = mappedClassModels.filter { it.originClass.canonicalName == baseClassCanonicalName }
                if (candidates.size <= 1) {
                    return candidates.first()
                }

                val orderedCandidates = candidates.topSortWith(
                    compareBy { it.simpleName },
                ) { candidate ->
                    val simpleDependencyNames = listOfNotNull(candidate.superclassConstructor?.constructorName) + candidate.implements
                    candidates.filter { it.simpleName in simpleDependencyNames }
                }

                return orderedCandidates.first()
            }

            val immediateBaseChildren = baseClass.immediateChildren.filter { it != baseClassName }
            val typeCaseMappings = immediateBaseChildren.associateWith {
                getMappedModelForBaseClass(it)
            }

            val properMappedModel = mappedClassModels
                .filter { it.originClass == baseClass && it.simpleName != mappedNodeClassModel.simpleName }
                .let {
                    check(it.size <= 1)
                    it.firstOrNull()
                }

            val conversion: (GqlExpr.Value) -> KtExpr.Value = { gqlExprValue: GqlExpr.Value ->
                KtExpr.Value { target, ctx ->
                    val nodeValueString = gqlExprValue.render(ctx)
                    val nodeValueLines = nodeValueString.lines()

                    val switchValue = if (nodeValueLines.size <= 1) {
                        target.appendLine("when ($nodeValueString) {")
                        nodeValueString
                    } else {
                        val switchVarName = "node${ctx.depth}"
                        target.append("val $switchVarName = ")
                        nodeValueLines.forEach(target::appendLine)
                        target.appendLine("when ($switchVarName) {")
                        switchVarName
                    }

                    target.indent {
                        typeCaseMappings.forEach { (childClassCanonicalName, _) ->
                            val parameterTypeString = Class.forName(childClassCanonicalName).kotlin.starProjectedType.toString()
                            append("is ")
                            append(parameterTypeString)
                            append(" -> ")

                            val baseType = parseGqlTypeString(parameterTypeString, false)
                            val innerTransformer = getFieldValueTransformerOverTypes(baseType)
                            val innerApplied = innerTransformer.expr.apply(GqlExpr.Value(switchValue))
                            innerApplied.renderInto(this, ctx)
                            appendLine()
                        }
                        // proper
                        append("else -> ")
                        if (properMappedModel == null) {
                            appendLine("throw IllegalStateException(\"Unrecognized subclass case\")")
                        } else {
                            val implTypeNode = providedImplTypeNodes[baseClassName]!!
                            val innerApplied = implTypeNode.expr.apply(GqlExpr.Value(switchValue))
                            innerApplied.renderInto(this, ctx)
                            appendLine()
                        }
                    }

                    target.appendLine("}")
                }
            }

            getTypeAlignmentTransformer(conversion)
                .also {
                    typeMappingCache[baseClassName] = Optional.of(it)
                }
        }
    }

    fun getObjectValueNode(
        mappedNodeClassModel: MappedNodeClassModel,
        objectValueSymbol: String,
    ): CNode<DType.GqlSchema.GqlType.Type, KtExpr.Value> {
        val converter = getTypeAlignmentTransformer(mappedNodeClassModel)
        return CNode<DType.GqlSchema.GqlType.Type, KtExpr.Value>(
            DType.GqlSchema.GqlType.Type,
            converter.expr.apply(GqlExpr.Value(objectValueSymbol))
        )
    }

    fun convertObjectValue(
        mappedNodeClassModel: MappedNodeClassModel,
        objectValueSymbol: String,
    ): String {
        val stringBuilder = StringBuilder()
        getObjectValueNode(mappedNodeClassModel, objectValueSymbol)
            .expr
            .renderInto(stringBuilder, RenderPathContext(0))
        return stringBuilder.toString()
    }

    companion object {

        private val primitiveTypeMapping = mapOf(
            "boolean" to "Boolean",
            "int" to "Int",
            "java.lang.String" to "String",
            "java.math.BigDecimal" to "Double",
            "java.math.BigInteger" to "Int",
        )

        private val dataClassTypeMapping = mapOf(
            "graphql.language.Argument" to "GraphQlLanguageNodeArgument",
            "graphql.language.Comment" to "GraphQlLanguageNodeComment",
            "graphql.language.Definition" to "GraphQlLanguageNodeDefinition",
            "graphql.language.Description" to "GraphQlLanguageNodeDescription",
            "graphql.language.Directive" to "GraphQlLanguageNodeDirective",
            "graphql.language.Document" to "GraphQlLanguageNodeDocument",
            "graphql.language.DirectiveLocation" to "GraphQlLanguageNodeDirectiveLocation",
            "graphql.language.EnumValueDefinition" to "GraphQlLanguageNodeEnumValueDefinition",
            "graphql.language.Field" to "GraphQlLanguageNodeField",
            "graphql.language.FieldDefinition" to "GraphQlLanguageNodeFieldDefinition",
            "graphql.language.InputValueDefinition" to "GraphQlLanguageNodeInputValueDefinition",
            "graphql.language.ObjectField" to "GraphQlLanguageNodeObjectField",
            "graphql.language.OperationDefinition\$Operation" to "com.arbr.graphql.lang.common.GraphQlOperationValue",
            "graphql.language.OperationTypeDefinition" to "GraphQlLanguageNodeOperationTypeDefinition",
            "graphql.language.Selection" to "GraphQlLanguageNodeSelection",
            "graphql.language.SelectionSet" to "GraphQlLanguageNodeSelectionSet",
            "graphql.language.SourceLocation" to "GraphQlLanguageNodeSourceLocation",
            "graphql.language.Type" to "GraphQlLanguageNodeType",
            "graphql.language.TypeName" to "GraphQlLanguageNodeTypeName",
            "graphql.language.Value" to "GraphQlLanguageNodeValue",
            "graphql.language.Value" to "GraphQlLanguageNodeValue",
            "graphql.language.VariableDefinition" to "GraphQlLanguageNodeVariableDefinition",
        )

        fun getTransformedKtType(gqlType: GqlExpr.LiteralType): KtExpr.LiteralType {
            return when (gqlType) {
                is GqlExpr.LiteralType.AtomicType -> {
                    val baseTypeString = gqlType.render(RenderPathContext(0))
                    val ktTypeString = primitiveTypeMapping[baseTypeString]
                        ?: dataClassTypeMapping[baseTypeString]
                        ?: kotlin.run {
                            if (baseTypeString.startsWith("graphql.language.")) {
                                // hack
                                "GraphQlLanguageNode" + baseTypeString.drop("graphql.language.".length)
                            } else {
                                null
                            }
                        }
                        ?: throw IllegalStateException(baseTypeString)

                    KtExpr.LiteralType.AtomicType(ktTypeString)
                }

                is GqlExpr.LiteralType.NullableType<*> -> {
                    KtExpr.LiteralType.NullableType(getTransformedKtType(gqlType.innerType))
                }

                is GqlExpr.LiteralType.Erased1ParameterType<*> -> {
                    // We currently drop erased type parameters in the Kt equivalent types
                    // Could add a hidden type wrapper here if information is needed elsewhere
                    getTransformedKtType(gqlType.outerType)
                }

                is GqlExpr.LiteralType.ListType<*> -> {
                    KtExpr.LiteralType.ListType(getTransformedKtType(gqlType.innerType))
                }

                is GqlExpr.LiteralType.MapType<*, *> -> {
                    KtExpr.LiteralType.MapType(
                        getTransformedKtType(gqlType.innerKeyType),
                        getTransformedKtType(gqlType.innerValueType),
                    )
                }
            }
        }

        private fun parseGqlTypeString(typeTree: RuleTree): GqlExpr.LiteralType {
            return when (typeTree.value) {
                "java.util.List" -> GqlExpr.LiteralType.ListType(
                    parseGqlTypeString(typeTree.children[0])
                )

                "java.util.Map" -> GqlExpr.LiteralType.MapType(
                    parseGqlTypeString(typeTree.children[0]),
                    parseGqlTypeString(typeTree.children[1]),
                )

                else -> {
                    check(typeTree.children.size <= 1)

                    val childType = typeTree.children.firstOrNull()
                    if (childType == null) {
                        GqlExpr.LiteralType.AtomicType(
                            StringBuilder().run {
                                RuleTree.serializeRuleTree(
                                    this,
                                    typeTree,
                                    "<",
                                    ">",
                                    ", ",
                                )
                                toString()
                            }
                        )
                    } else {
                        check(childType.value == "*")
                        GqlExpr.LiteralType.Erased1ParameterType(
                            GqlExpr.LiteralType.AtomicType(typeTree.value)
                        )
                    }
                }
            }
        }

        fun parseGqlTypeString(
            gqlTypeString: String,
            nullable: Boolean,
        ): GqlExpr.LiteralType {
            val typeTree = RuleTree.parseRuleTree(gqlTypeString, '<', '>', ',')
            val parsedInnerType = parseGqlTypeString(typeTree)
            return if (nullable) {
                GqlExpr.LiteralType.NullableType(parsedInnerType)
            } else {
                parsedInnerType
            }
        }
    }
}

