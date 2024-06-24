package com.arbr.graphql.converter

import com.arbr.graphql.converter.expressions.IndentAppendableWrapper
import com.arbr.graphql.converter.model.MappedKotlinTopLevelObjectSpecifier
import com.arbr.graphql.converter.model.MappedNodeClassModel
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

private fun appendage(doAppend: Appendable.() -> Unit): (Appendable) -> Unit = { app ->
    doAppend(app)
}

fun Appendable.indent(doAppend: Appendable.() -> Unit) = doAppend(IndentAppendableWrapper(this))

internal class LangToKtConverter(
    outputSourceDir: Path,
) {
    private val sourceSetPath = Paths.get(outputSourceDir.toString(), SOURCE_SET_SUB_PATH)

    private fun writeKtSource(
        mappedNodeClassModel: MappedNodeClassModel,
    ): Path {
        val immediateOutDir = mappedNodeClassModel.getTargetDir(sourceSetPath)
        if (!immediateOutDir.exists()) {
            Files.createDirectories(immediateOutDir)
        }

        val outFile = mappedNodeClassModel.getTargetFilePath(sourceSetPath).toFile()
        if (outFile.exists()) {
            outFile.delete()
        }
        outFile.createNewFile()

        val classDirectives = mappedNodeClassModel.objectKind.getModifierString()
        val usesConstructor = mappedNodeClassModel.objectKind.usesConstructor()
        val isInterface = mappedNodeClassModel.objectKind == MappedKotlinTopLevelObjectSpecifier.SEALED_INTERFACE

        val constructorAppendage = appendage {
            if (usesConstructor) {
                appendLine("(")
                indent {
                    mappedNodeClassModel.fieldModels.forEach { fieldModel ->
                        if (fieldModel.isOverride) {
                            append("override ")
                        }
                        append("val ")
                        append(fieldModel.name)
                        append(": ")
                        append(fieldModel.typeName)
                        if (fieldModel.nullable) {
                            append("?")
                        }
                        appendLine(",")
                    }
                }
                append(")")
            }
        }

        val rootInterface = "GraphQlLanguageNodeBase"
        val inheritanceAppendage = appendage {
            append(": ")

            val superConstructor = mappedNodeClassModel.superclassConstructor
            if (superConstructor != null) {
                append(superConstructor.constructorName)
                append("(")
                superConstructor.arguments.forEachIndexed { i, arg ->
                    append(arg)
                    if (i < superConstructor.arguments.size - 1) {
                        append(", ")
                    }
                }
                append("), ")
            }

            append(mappedNodeClassModel.implements.firstOrNull() ?: rootInterface)
            mappedNodeClassModel.implements.drop(1).forEach { implementee ->
                append(", ")
                append(implementee)
            }
        }

        val bodyAppendage = appendage {
            if (isInterface) {
                appendLine(" {")
                indent {
                    val hiddenSuperFields = mappedNodeClassModel.fieldModels
                        .filter { it.isOverride }

                    hiddenSuperFields
                        .forEach {
                            val mappedTypeName = it.typeName + (if (it.nullable) "?" else "")
                            appendLine("// override val ${it.name}: $mappedTypeName")
                        }

                    if (hiddenSuperFields.isNotEmpty()) {
                        appendLine()
                    }

                    mappedNodeClassModel.fieldModels
                        .filter { !it.isOverride }
                        .forEach {
                            val mappedTypeName = it.typeName + (if (it.nullable) "?" else "")
                            appendLine("val ${it.name}: $mappedTypeName")
                        }
                }

                append("}")
            }
        }

        val writer = outFile.bufferedWriter()

        writer.apply {
            append("package ")
            appendLine(mappedNodeClassModel.packageName)
            appendLine()
            append(classDirectives)
            append(" ")
            append(mappedNodeClassModel.simpleName)
            constructorAppendage(this)
            inheritanceAppendage(this)
            bodyAppendage(this)
        }.flush()

        val outFilePath = outFile.toPath()
        logger.info("Wrote $classDirectives ${mappedNodeClassModel.simpleName} to $outFilePath")
        return outFilePath
    }

    private fun writeLibClassesToKtDataClasses(
        classModels: List<MappedNodeClassModel>,
    ) {
        // Prep directories
        if (!sourceSetPath.exists()) {
            Files.createDirectories(sourceSetPath)
        }

        Files.list(sourceSetPath)
            .forEach {
                if (it.isRegularFile()) {
                    it.deleteIfExists()
                }
            }

        classModels.forEach(this::writeKtSource)
    }

    fun convert(classModels: List<MappedNodeClassModel>) {
        writeLibClassesToKtDataClasses(classModels)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LangToKtConverter::class.java)

        private const val SOURCE_SET_SUB_PATH = "src/main/kotlin"
    }
}
