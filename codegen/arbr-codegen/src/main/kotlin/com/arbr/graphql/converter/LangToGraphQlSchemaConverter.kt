package com.arbr.graphql.converter

import com.arbr.graphql.converter.model.GraphQlSchemaFieldModel
import com.arbr.graphql.converter.model.GraphQlSchemaTypeModel
import com.arbr.graphql.converter.model.MappedNodeClassModel
import com.arbr.graphql.converter.regex.RegexNamedReplacementRule
import com.arbr.graphql.converter.regex.RegexReplacementNumberedRule
import com.arbr.graphql.converter.regex.Regexes
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

internal class LangToGraphQlSchemaConverter(
    private val outputSourceDir: Path,
) {
    private val targetDir = Paths.get(outputSourceDir.toString(), "src/main/resources/schema")
    private val targetFilePath = Paths.get(targetDir.toString(), "graphql_meta.graphql")

    private fun ruleReplace(s: String): String {
        return Regexes.replaceByRules(
            s,
            allParseRules,
        )
    }

    private fun ruleReplaceTransform(parsedTypeName: String): String {
        return Regexes.replaceByRules(
            parsedTypeName,
            transformationRules,
        )
    }

    private fun transformNodeClassName(
        className: String,
    ): String {
        val prefix = "GraphQlLanguageNode"
        return if (className.startsWith(prefix)) {
            className.drop(prefix.length)
        } else {
            className
        }
    }

    private fun nodeClassModelsToGraphQLSchema(
        classModels: List<MappedNodeClassModel>,
    ): List<GraphQlSchemaTypeModel> {
        return classModels
            .map { transformNodeClassName(it.simpleName) to it }
            .distinctBy { it.first }
            .sortedBy { it.first }
            .map { (name, classModel) ->
                GraphQlSchemaTypeModel(
                    name,
                    classModel.fieldModels.map { fieldModel ->
                        val parsedTypeName = ruleReplace(fieldModel.typeName)
                        val transformedTypeName = ruleReplaceTransform(parsedTypeName)

                        GraphQlSchemaFieldModel(
                            fieldModel.name,
                            transformedTypeName,
                        )
                    },
                )
            }
    }

    private fun writeSchemaModels(
        schemaModels: List<GraphQlSchemaTypeModel>,
    ) {
        val targetFile = targetFilePath.toFile()
        if (targetFile.exists()) {
            targetFile.delete()
        }
        targetFile.createNewFile()

        val schemaModelNames = schemaModels.map { it.name }.toSet()
        val constantTypeNames = headerConstantTypes
            .filter { it !in schemaModelNames }
            .distinct()

        targetFile.bufferedWriter().apply {
            appendLine()
            constantTypeNames.forEach { typeName ->
                // TODO: Add real types
                appendLine("type $typeName {")
                appendLine("    name: String")
                appendLine("    value: String")
                appendLine("}")
                appendLine()
            }

            schemaModels.forEach { schemaModel ->
                appendLine()
                appendLine("type ${schemaModel.name} {")
                schemaModel.fields.forEach { fm ->
                    appendLine("    ${fm.name}: ${fm.typeName}")
                }
                appendLine("}")
                toString()
            }
        }.flush()
    }

    fun convert(classModels: List<MappedNodeClassModel>): List<GraphQlSchemaTypeModel> {
        val schemaModels: List<GraphQlSchemaTypeModel> = nodeClassModelsToGraphQLSchema(classModels)

        if (!targetDir.exists()) {
            Files.createDirectories(targetDir)
        }

        logger.debug("Output models:")
        schemaModels.forEach {
            logger.debug("\n")
            logger.debug(it.name)
            it.fields.forEach { f ->
                logger.debug(f.name + " : " + f.typeName)
            }
        }

        writeSchemaModels(schemaModels)
        return schemaModels
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LangToGraphQlSchemaConverter::class.java)

        private val basicParseRules = listOf(
            RegexReplacementNumberedRule(
                Regex("(GraphQlLanguageNode)([a-zA-Z0-9._]+)"),
                { "Type" },
                { "[$it]" },
            ),
            RegexNamedReplacementRule(
                Regex("(?<outerType>[^<>]+)<(?<innerType>[^<>]+)>"),
            ) { _, namedMatches ->
                val outerType = namedMatches["outerType"]!!.value
                val innerTypeString = namedMatches["innerType"]!!.value

                "$outerType[$innerTypeString]"
            },
        )

        /**
         * Kt -> (Intermediate) GraphQl
         */
        private val primitiveTypeNameIntermediateMap = mapOf(
            "Boolean" to "GQL_BOOLEAN",
            "Double" to "GQL_FLOAT",
            "Int" to "GQL_INT",
            "String" to "GQL_STRING",
            "com.arbr.graphql.lang.common.GraphQlOperationValue" to "GQL_OPERATION",
        )
        private val primitiveRules = primitiveTypeNameIntermediateMap.map { (ktTypeName, gqlTypeName) ->
            val escapedName = Regex.escape(ktTypeName)
            RegexReplacementNumberedRule(
                Regex("([,<\\s\\[\\]]|^)($escapedName)([,>\\s\\[\\]]|\$)"),
                { it },
                {
                    "Primitive[$gqlTypeName]"
                },
                { it },
            )
        }

        private val allParseRules = basicParseRules + primitiveRules

        private val primitiveTypeTransformationMap = mapOf(
            "GQL_BOOLEAN" to "Boolean",
            "GQL_FLOAT" to "Float",
            "GQL_INT" to "Int",
            "GQL_STRING" to "String",
            "GQL_OPERATION" to "Operation",
        )
        private val primitiveTransformationRules = primitiveTypeTransformationMap.map { (irTypeName, gqlTypeName) ->
            val escapedName = Regex.escape(irTypeName)
            RegexReplacementNumberedRule(
                Regex("(Primitive\\[$escapedName])"),
                { gqlTypeName },
            )
        }

        @Suppress("SameParameterValue")
        private fun balanceBrackets(
            inputString: String,
            bracketCharOpen: Char,
            bracketCharClose: Char,
        ): String {
            var startBracketIndex: Int? = null
            var bracketCt = 0
            var i = 0
            while ((bracketCt > 0 || startBracketIndex == null) && i < inputString.length) {
                bracketCt += when (inputString[i]) {
                    bracketCharOpen -> {
                        if (startBracketIndex == null) {
                            startBracketIndex = i
                        }
                        1
                    }

                    bracketCharClose -> -1
                    else -> 0
                }
                i++
            }
            if (bracketCt > 0 || startBracketIndex == null) {
                throw IllegalStateException("Unbalanced brackets in: $inputString")
            }

            return inputString.substring(startBracketIndex + 1, i - 1)
        }

        private val transformationRules = primitiveTransformationRules + listOf(
            // No map type in GraphQL but we need for additionalData
            RegexReplacementNumberedRule(
                Regex("(Map\\[String, String])"),
                { "JsonObject" },
            ),
            // Needs to balance brackets - glob to end of string and find first nontrivial zero
            RegexNamedReplacementRule(
                Regex("(?<pre>.*)List(?<inner>\\[.*$)"),
            ) { _, m ->
                val pre = m["pre"]!!.value
                val inner = m["inner"]!!.value
                pre + balanceBrackets(inner, '[', ']')
            },
            RegexNamedReplacementRule(
                Regex("(?<pre>.*)Type(?<inner>\\[.*$)"),
            ) { _, m ->
                val pre = m["pre"]!!.value
                val inner = m["inner"]!!.value
                pre + balanceBrackets(inner, '[', ']')
            }
        )

        private val headerConstantTypes = listOf(
            "Comment",
            "SourceLocation",
            "Definition",
            "Description",
            "Operation",
            "JsonObject",
        )
    }
}
