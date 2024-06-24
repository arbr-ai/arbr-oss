package com.arbr.codegen.base.generator.targets.spec

import com.arbr.codegen.base.generator.DisplayRootModel
import com.arbr.codegen.base.generator.targets.GeneratorTargetOutput
import com.arbr.codegen.base.generator.targets.GeneratorTargetOutputCollection
import java.nio.file.Paths

internal sealed interface GeneratorTargetFileSpecifier {

    fun specifyOutputLocation(displayRootModel: DisplayRootModel, output: String): GeneratorTargetOutput

    private fun packageDomainToPath(packageDomain: String): String {
        if (packageDomain.isBlank()) {
            return packageDomain
        }

        val tokens = packageDomain.split(".")
        return Paths.get(
            tokens.first(),
            *(tokens.drop(1)).toTypedArray()
        ).toString()
    }

    fun kotlinSourcePackagePath(
        output: String,
    ): String {
        val prefix = "package "
        val packageDeclaration = output.lines().firstOrNull {
            it.startsWith(prefix, ignoreCase = true)
        } ?: throw Exception("Kotlin output with no package declaration")

        val packageDomain = packageDeclaration.drop(prefix.length).trimEnd(';').trim()
        return packageDomainToPath(packageDomain)
    }

    fun kotlinSourceTopLevelObject(
        output: String,
    ): String {
        // Should probably use a parser here
        val modifiers = listOf(
            "class",
            "interface",
            "object",
        )
        // Strict class naming
        val classDeclRegexes = modifiers.map {
            Regex("$it\\s+([A-Z][a-zA-Z0-9]*)")
        }

        val toplevelDeclaration = output.lines().firstNotNullOfOrNull { line ->
            classDeclRegexes.firstNotNullOfOrNull {
                it.find(line)?.groupValues?.getOrNull(1)
            }
        } ?: throw Exception("Kotlin output with no top-level object")

        return toplevelDeclaration
    }

    data object KtSourcePackageDerived : GeneratorTargetFileSpecifier {
        override fun specifyOutputLocation(displayRootModel: DisplayRootModel, output: String): GeneratorTargetOutput {
            val packagePath = kotlinSourcePackagePath(output)
            val topLevelObject = kotlinSourceTopLevelObject(output)
            val fileName = "$topLevelObject.kt"
            val pathInProject = Paths.get(packagePath, fileName).toString()
            return GeneratorTargetOutput(GeneratorTargetOutputCollection.PROJECT, pathInProject, output)
        }
    }
}