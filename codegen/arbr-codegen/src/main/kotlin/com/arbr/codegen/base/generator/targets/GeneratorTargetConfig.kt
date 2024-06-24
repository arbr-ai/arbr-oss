package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetFileSpecifier
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetOutputGenerator
import com.arbr.codegen.base.generator.targets.spec.GeneratorTargetReplicationStrategy
import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Paths

internal object GeneratorTargetConfig {

    private val templateDirCandidates = listOf(
        "src/main/resources/templates",
        "templates",
        ".",
    )

    private fun getClassLoader(): ClassLoader {
        return Thread.currentThread().contextClassLoader
    }

    private fun findResourceUrl(fileName: String): URL? {
        val contextClassLoader = getClassLoader()
        return templateDirCandidates.firstNotNullOfOrNull { dir ->
            val templatePath = Paths.get(dir, fileName).toString()

            contextClassLoader.getResource(
                templatePath
            )
        }
    }

    fun getTemplateUrl(templateFileName: String): URL {
        return findResourceUrl(templateFileName)
            ?: throw FileNotFoundException("No template file $templateFileName found in classpath")
    }

    fun generatorTargets(): List<GeneratorTarget> {
        val contextClassLoader = getClassLoader()
        val manifestUrl = contextClassLoader.getResource("template-manifest.txt")

        val manifestText = manifestUrl?.readText() ?: return emptyList()
        val manifestGenerators = manifestText.lines()
            .filter { it.isNotBlank() }
            .map {
                Paths.get(it).last().toString()
            }
            .mapNotNull { templateFileName ->
                when {
                    templateFileName.endsWith(".kttmpl") -> {
                        findResourceUrl(
                            templateFileName
                        )?.let {
                            GeneratorTargetOutputGenerator.ArbrTemplate(templateFileName)
                        }
                    }
                    templateFileName.endsWith(".mustache") -> {
                        findResourceUrl(
                            templateFileName
                        )?.let {
                            GeneratorTargetOutputGenerator.MustacheTemplate(templateFileName)
                        }
                    }
                    else -> {
                        throw IllegalArgumentException("Invalid template in manifest: $templateFileName")
                    }
                }
            }

        return manifestGenerators.map {
            GeneratorTarget(
                GeneratorTargetReplicationStrategy.PerTable,
                GeneratorTargetFileSpecifier.KtSourcePackageDerived,
                GeneratorTargetModelMapper.Id,
                it
            )
        }
    }

}
