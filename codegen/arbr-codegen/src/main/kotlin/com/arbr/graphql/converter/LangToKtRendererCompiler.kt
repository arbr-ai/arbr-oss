package com.arbr.graphql.converter

import com.arbr.graphql.converter.expressions.CNode
import com.arbr.graphql.converter.expressions.DType
import com.arbr.graphql.converter.expressions.KtExpr
import com.arbr.graphql.converter.expressions.RenderPathContext
import com.arbr.graphql.converter.model.MappedNodeClassModel
import com.arbr.graphql.converter.util.topSortWith
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

@Suppress("SameParameterValue")
internal class LangToKtRendererCompiler(
    private val buildDir: Path,
    private val mappedModels: List<MappedNodeClassModel>,
) {
    private val targetDir =
        Paths.get(buildDir.toString(), "src/main/kotlin/com.arbr.graphql.lang.common")
    private val className = "GraphQlNodeRenderer"
    private val targetFilePath = Paths.get(targetDir.toString(), "$className.kt")

    private fun getRenderMethodHeader(
        mappedNodeModel: MappedNodeClassModel,
        isOverride: Boolean,
    ): KtExpr.MethodHeader {
        val baseTypeAsKt = KtExpr.LiteralType.AtomicType(mappedNodeModel.simpleName)
        val returnTypeKt = KtExpr.LiteralType.NullableType(
            KtExpr.LiteralType.AtomicType(
                "AppendableBlock"
            )
        )

        val methodName = "render${mappedNodeModel.simpleName}"
        val paramName = "node" // We are free to choose this

        return KtExpr.MethodHeader(
            if (isOverride) listOf("override") else listOf("open"),
            methodName,
            listOf(
                KtExpr.MethodParameter(paramName, baseTypeAsKt),
            ),
            returnTypeKt,
        )
    }

    private fun getMethodImplNodeNull(
        mappedNodeModel: MappedNodeClassModel,
    ): CNode<DType.GqlSchema.GqlType.Type, KtExpr.MethodImpl> {
        val methodHeader = getRenderMethodHeader(
            mappedNodeModel,
            isOverride = false
        )
        check(methodHeader.methodParameters.size == 1)

        val methodImplExpr = KtExpr.MethodImpl(
            methodHeader,
            emptyList(),
            KtExpr.Value("null"),
        )

        return CNode(
            DType.GqlSchema.GqlType.Type,
            methodImplExpr
        )
    }

    private fun getNodeProcessorMethodImpl(
        mappedModelsOrdered: List<MappedNodeClassModel>,
    ): CNode<DType.GqlSchema.GqlType.Type, KtExpr.MethodImpl> {
        val methodName = "processNode"
        val paramName = "traversalState"
        val paramType = KtExpr.LiteralType.AtomicType(
            "GraphQlLanguageNodeTraversalState"
        )

        val methodHeader =  KtExpr.MethodHeader(
            listOf("override"),
            methodName,
            listOf(
                KtExpr.MethodParameter(paramName, paramType),
            ),
            null,
        )

        val switchExpr = KtExpr.Value { appendable, _ ->
            appendable.apply {
                appendLine("if (traversalState.context.phase != GraphQlLanguageNodeTraversalContextPhase.ENTER) {")
                indent {
                    appendLine("return")
                }
                appendLine("}")
                appendLine("val nodeId = traversalState.context.nodeId")
                appendLine("val parentNodeId = traversalState.context.parentNodeId")
                appendLine("if (parentNodeId in touchedNodeIds) {")
                indent {
                    appendLine("touchedNodeIds.add(nodeId)")
                    appendLine("return")
                }
                appendLine("}")
                appendLine()
                appendLine("val node = traversalState.node")
                mappedModelsOrdered.forEach { model ->
                    val functionName = "render${model.simpleName}"

                    appendLine("if (node is ${model.simpleName}) {")
                    indent {
                        appendLine("val appendBlock = ${functionName}(node)")
                        appendLine("if (appendBlock != null) {")
                        indent {
                            appendLine("touchedNodeIds.add(nodeId)")
                            appendLine("appendBlock(writer)")
                            appendLine("return")
                        }
                        appendLine("}")
                    }
                    appendLine("}")
                    appendLine()
                }
            }
        }

        val methodImplExpr = KtExpr.MethodImpl(
            methodHeader,
            listOf(switchExpr),
            null,
        )

        return CNode(
            DType.GqlSchema.GqlType.Type,
            methodImplExpr
        )
    }

    fun compileRenderer() {
        val mustacheFactory = DefaultMustacheFactory()
        val dataModelMustache: Mustache = mustacheFactory.compile(
            "src/main/resources/templates/GraphQlNodeRenderer.kt.mustache"
        )

        if (!targetDir.exists()) {
            Files.createDirectories(targetDir)
        }
        val targetFile = targetFilePath.toFile()

        logger.info("Sorting models...")
        val mappedModelsOrdered = mappedModels.topSortWith(
            compareBy<MappedNodeClassModel> { it.simpleName }.reversed(),
        ) { candidate ->
            val simpleDependencyNames =
                listOfNotNull(candidate.superclassConstructor?.constructorName) + candidate.implements
            mappedModels.filter { it.simpleName in simpleDependencyNames }
        }
            .asReversed()
        logger.info("Got ordered models")

        val mappedInterfaceDeclarations = mappedModelsOrdered.map(this::getMethodImplNodeNull)
        logger.info("Got null method impls")

        val processNodeMethodImpl = getNodeProcessorMethodImpl(mappedModelsOrdered)
        logger.info("Got NP method impls")

        val writer = dataModelMustache.execute(
            targetFile.bufferedWriter(),
            Unit,
        )
        writer.flush()

        val ctx = RenderPathContext(0)
        writer.apply {
            indent {
                mappedInterfaceDeclarations.forEach { decl ->
                    decl.expr.renderInto(this, ctx)
                    appendLine()
                }
                appendLine()
                processNodeMethodImpl.expr.renderInto(this, ctx)
            }
            appendLine("}")
        }.flush()

        logger.info("Wrote $targetFilePath")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LangToKtRendererCompiler::class.java)
    }

}
