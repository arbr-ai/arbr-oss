package com.arbr.graphql.converter

import com.arbr.graphql.converter.expressions.*
import com.arbr.graphql.converter.model.MappedNodeClassModel
import com.arbr.graphql.converter.util.StringUtils
import com.arbr.graphql.converter.util.topSortWith
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.reflect.full.starProjectedType

internal class LangToKtTransformerCompiler(
    private val buildDir: Path,
    private val mappedModels: List<MappedNodeClassModel>,
) {
    private val targetDir =
        Paths.get(buildDir.toString(), "src/main/kotlin/com.arbr.graphql.lang.common")
    private val packageName = "com.arbr.graphql.lang.common"
    private val targetFilePath = Paths.get(targetDir.toString(), "GraphQlLanguageNodeTransformer.kt")

    private fun mappedClassExpectedName(baseClassSimpleName: String): String {
        val simpleName = StringUtils.getCaseSuite(baseClassSimpleName).titleCase
        return "GraphQlLanguageNode$simpleName"
    }

    private fun getConversionMethodHeader(
        mappedNodeModel: MappedNodeClassModel,
    ): KtExpr.MethodHeader {
        // Small hack to get the erased type name
        val gqlTypeString = Class.forName(mappedNodeModel.originClass.canonicalName).kotlin.starProjectedType.toString()
        val baseType = LangToKtFieldTypeCompiler.parseGqlTypeString(gqlTypeString, false)
        val baseTypeAsKt = KtExpr.LiteralType.AtomicType(baseType.render(RenderPathContext(0)))
        val returnTypeKt = LangToKtFieldTypeCompiler.getTransformedKtType(
            baseType
        )

        // Compute modifier
        val expectedMappedClassName = mappedClassExpectedName(mappedNodeModel.originClass.simpleName)
        check(mappedNodeModel.simpleName.startsWith(expectedMappedClassName))
        val modifier = mappedNodeModel.simpleName.drop(expectedMappedClassName.length)

        val methodName = "convert${mappedNodeModel.originClass.simpleName}${modifier}"
        val paramName = "node" // We are free to choose this

        return KtExpr.MethodHeader(
            emptyList(),
            methodName,
            listOf(
                KtExpr.MethodParameter(paramName, baseTypeAsKt),
            ),
            returnTypeKt,
        )
    }

    private fun getMethodNode(
        fieldTypeCompiler: LangToKtFieldTypeCompiler,
        mappedNodeModel: MappedNodeClassModel,
    ): CNode<DType.GqlSchema.GqlType.Type, KtExpr.MethodImpl> {
        val methodHeader = getConversionMethodHeader(mappedNodeModel)
        check(methodHeader.methodParameters.size == 1)

        val nodeParameterName = methodHeader.methodParameters.first().name
        val convertedObjectValueNode: CNode<DType.GqlSchema.GqlType.Type, KtExpr.Value> =
            fieldTypeCompiler.getObjectValueNode(mappedNodeModel, nodeParameterName)

        val methodImplExpr = KtExpr.MethodImpl(
            methodHeader,
            emptyList(),
            convertedObjectValueNode.expr,
        )

        return CNode(
            DType.GqlSchema.GqlType.Type,
            methodImplExpr
        )
    }

    fun compileTransformer() {
        /*
            override fun visitArgument(node: Argument, data: TraverserContext<Node<Node<*>>>): TraversalControl {
                ///
            }
            override fun visit[B<T>::name](node: [B<T>::name], data: TraverserContext<Node<Node<*>>>): TraversalControl {
                /// ((B<T> -> M<T>) node)
                /// ((l b \in B<T>. (M<T>::constructor b)) node)
                /// (M<T>::constructor B<T> node)
            }
         */

        // Include only top-level nodes for each base type, no sub-implementations
        val providedTypeNodes = mappedModels
            .groupBy { it.originClass.canonicalName }
            .mapValues { (_, mappedNodesForBaseClass) ->
                if (mappedNodesForBaseClass.size <= 1) {
                    mappedNodesForBaseClass.first()
                } else {
                    val orderedCandidates = mappedNodesForBaseClass.topSortWith(
                        compareBy { it.simpleName },
                    ) { candidate ->
                        val simpleDependencyNames =
                            listOfNotNull(candidate.superclassConstructor?.constructorName) + candidate.implements
                        mappedNodesForBaseClass.filter { it.simpleName in simpleDependencyNames }
                    }

                    orderedCandidates.first()
                }
            }
            .mapValues { (canonicalName, mappedNodeClassModel) ->
                val methodHeader = getConversionMethodHeader(mappedNodeClassModel)

                CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>(
                    DType.GqlSchema.GqlType.Type,
                    Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                        KtExpr.Value { t, ctx ->
                            t.append(methodHeader.methodName)
                            t.append("(")
                            gqlExprValue.renderInto(t, ctx)
                            t.append(")")
                        }
                    }
                )
            }

        val providedImplTypeNodes = mappedModels
            .groupBy { it.originClass.canonicalName }
            .filterValues { it.size > 1 }
            .mapValues { (_, mappedNodesForBaseClass) ->
                val orderedCandidates = mappedNodesForBaseClass.topSortWith(
                    compareBy { it.simpleName },
                ) { candidate ->
                    val simpleDependencyNames =
                        listOfNotNull(candidate.superclassConstructor?.constructorName) + candidate.implements
                    mappedNodesForBaseClass.filter { it.simpleName in simpleDependencyNames }
                }

                orderedCandidates[1]
            }
            .mapValues { (canonicalName, mappedNodeClassModel) ->
                val methodHeader = getConversionMethodHeader(mappedNodeClassModel)

                CNode<DType.GqlSchema.GqlType.Type, Expr.Fn<GqlExpr.Value, KtExpr.Value>>(
                    DType.GqlSchema.GqlType.Type,
                    Expr.Fn<GqlExpr.Value, KtExpr.Value> { gqlExprValue ->
                        KtExpr.Value { t, ctx ->
                            t.append(methodHeader.methodName)
                            t.append("(")
                            gqlExprValue.renderInto(t, ctx)
                            t.append(")")
                        }
                    }
                )
            }

        val fieldTypeCompiler = LangToKtFieldTypeCompiler(
            mappedModels,
            providedTypeNodes,
            providedImplTypeNodes,
        )

//        val mappedModelsToImplement = mappedModels
//            .filter { model ->
//                // Do not implement if there is another instantiable type
//                model.objectKind.isInstantiable() || mappedModels.none {
//                    it.originClass == model.originClass && it.objectKind.isInstantiable()
//                }
//            }

        val mappedModelsToImplement = mappedModels
            .groupBy { it.originClass }
            .flatMap { (baseClass, mappedModelsForBaseClass) ->
                if (mappedModelsForBaseClass.size <= 1) {
                    mappedModelsForBaseClass
                } else {
                    val expectedMappedClassName = mappedClassExpectedName(baseClass.simpleName)
                    check(mappedModelsForBaseClass.all { it.simpleName.startsWith(expectedMappedClassName) })
                    val modifiers = mappedModelsForBaseClass.map { it.simpleName.drop(expectedMappedClassName.length) }
                    check(modifiers.size == modifiers.distinct().size)

                    mappedModelsForBaseClass
                }
            }

        if (!targetDir.exists()) {
            Files.createDirectories(targetDir)
        }
        val targetFile = targetFilePath.toFile()
        targetFile.bufferedWriter().also { targetAppendable ->
            targetAppendable.appendLine("package $packageName")
            targetAppendable.appendLine("")
            targetAppendable.appendLine("import com.arbr.graphql.lang.node.*")
            targetAppendable.appendLine("")
            targetAppendable.appendLine("class GraphQlLanguageNodeTransformer {")
            targetAppendable.appendLine("")

            val wrappedAppendable = IndentAppendableWrapper(targetAppendable)
            mappedModelsToImplement.forEach { mappedModel ->
                val methodModel = getMethodNode(fieldTypeCompiler, mappedModel)
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
        private val logger = LoggerFactory.getLogger(LangToKtTransformerCompiler::class.java)
    }

}
