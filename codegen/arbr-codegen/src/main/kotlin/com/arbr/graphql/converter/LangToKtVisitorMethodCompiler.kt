package com.arbr.graphql.converter

import com.arbr.graphql.converter.expressions.*
import com.arbr.graphql.converter.model.MappedNodeClassModel
import com.arbr.graphql.converter.util.StringUtils
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import graphql.language.NodeVisitor
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.reflect.full.starProjectedType

internal class LangToKtVisitorMethodCompiler(
    private val buildDir: Path,
    private val mappedModels: List<MappedNodeClassModel>,
) {
    private val targetDir = Paths.get(buildDir.toString(), "src/main/kotlin/com.arbr.graphql.lang.common")
    private val targetFilePath = Paths.get(targetDir.toString(), "CompilerProcessNodeVisitor.kt")

    private val fieldTypeCompiler = LangToKtFieldTypeCompiler(mappedModels)

    private fun getBaseVisitorMethodModels(): List<BaseVisitorMethodModel> {
        val visitorClass = NodeVisitor::class.java
        return visitorClass.declaredMethods.map {
            val parameterNames = it.parameters.map { p -> p.name }
            val parameterTypes = it.genericParameterTypes.toList()
            check(parameterTypes.size == 2)
            check(parameterNames.size == 2)
            check(parameterNames[0] == "node")
            check(parameterNames[1] == "data")

            BaseVisitorMethodModel(
                it.name,
                parameterNames[0],
                parameterTypes[0],
                parameterNames[1],
                parameterTypes[1],
            )
        }
    }

    private fun getMethodNode(
        visitorMethodModel: BaseVisitorMethodModel,
        mappedNodeModel: MappedNodeClassModel,
    ): CNode<DType.GqlSchema.GqlType.Type, KtExpr.MethodImpl> {
        val conversionMethodName = visitorMethodModel.name.replace("visit", "convert")

        // Small hack to get the erased type name
        val gqlTypeString = Class.forName(mappedNodeModel.originClass.canonicalName).kotlin.starProjectedType.toString()
        val baseType = LangToKtFieldTypeCompiler.parseGqlTypeString(gqlTypeString, false)
        val baseTypeAsKt = KtExpr.LiteralType.AtomicType(baseType.render(RenderPathContext(0)))
        val tcTypeKt = KtExpr.LiteralType.AtomicType(
            "graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>"
        )

        val nodeTypeEnumValue = StringUtils.getCaseSuite(mappedNodeModel.simpleName).screamingSnakeCase

        // Hack: not actually a value
        val assignmentExpr = KtExpr.Value { t, ctx ->
            t.append("val converted = transformer.${conversionMethodName}(${visitorMethodModel.nodeParameterName})")
        }
        val handleExpr = KtExpr.Value { t, _ ->
            t.appendLine("val nodeType = GraphQlSimpleNodeType.${nodeTypeEnumValue}")
            t.appendLine("val context = visitNodeAndGetContext(nodeType, ${visitorMethodModel.tcParameterName})")
            t.appendLine("nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))")
        }

        val methodImplExpr = KtExpr.MethodImpl(
            KtExpr.MethodHeader(
                listOf("override"),
                visitorMethodModel.name,
                listOf(
                    KtExpr.MethodParameter(visitorMethodModel.nodeParameterName, baseTypeAsKt),
                    KtExpr.MethodParameter(visitorMethodModel.tcParameterName, tcTypeKt),
                ),
                KtExpr.LiteralType.AtomicType("graphql.util.TraversalControl"),
            ),
            listOf(
                assignmentExpr,
                handleExpr,
//                convertedObjectValueNode.expr,
            ),
            KtExpr.Value("graphql.util.TraversalControl.CONTINUE")
        )

        return CNode(
            DType.GqlSchema.GqlType.Type,
            methodImplExpr
        )
    }

    fun compileTransformer() {
        // Need to align classes and fields first
        val baseVisitorMethodModels = getBaseVisitorMethodModels()
        val pairedClassModels = baseVisitorMethodModels.associate {
            val canonicalName = (it.nodeParameterType as Class<*>).canonicalName
            val matchedNodeClassModel = mappedModels.filter { m ->
                m.originClass.canonicalName == canonicalName
                        && m.objectKind.isInstantiable()
            }
                .let { matches ->
                    check(matches.size == 1) {
                        if (matches.isEmpty()) {
                            "No matches for $canonicalName"
                        } else {
                            matches.joinToString("\n") { m -> m.toString() }
                        }
                    }
                    matches.first()
                }

            canonicalName to (
                    it to matchedNodeClassModel
                    )
        }

        val mustacheFactory = DefaultMustacheFactory()
        val dataModelMustache: Mustache = mustacheFactory.compile(
            "src/main/resources/templates/CompilerProcessNodeVisitor.kt.mustache"
        )

        if (!targetDir.exists()) {
            Files.createDirectories(targetDir)
        }
        val targetFile = targetFilePath.toFile()
        val writer = dataModelMustache.execute(
            targetFile.bufferedWriter(),
            Unit,
        )

        writer.also { targetAppendable ->
            val wrappedAppendable = IndentAppendableWrapper(targetAppendable)
            pairedClassModels.values.forEach { (baseVisitorMethodModel, mappedModel) ->
                val methodModel = getMethodNode(baseVisitorMethodModel, mappedModel)
                methodModel.expr.renderInto(
                    wrappedAppendable,
                    RenderPathContext(0)
                )
                wrappedAppendable.appendLine()
            }

            targetAppendable.appendLine("}")
        }.flush()

        logger.info("Wrote $targetFilePath")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LangToKtVisitorMethodCompiler::class.java)
    }
}
