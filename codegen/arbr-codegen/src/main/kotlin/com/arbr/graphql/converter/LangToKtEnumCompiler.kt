package com.arbr.graphql.converter

import com.arbr.graphql.converter.model.MappedNodeClassModel
import com.arbr.graphql.converter.util.StringUtils
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

internal class LangToKtEnumCompiler(
    private val buildDir: Path,
    private val mappedModels: List<MappedNodeClassModel>,
) {
    private val targetDir = Paths.get(buildDir.toString(), "src/main/kotlin/com.arbr.graphql.lang.common")
    private val targetFilePath = Paths.get(targetDir.toString(), "GraphQlSimpleNodeType.kt")

    data class TemplateModel(
        val nodeType: List<NodeTypeTemplateModel>,
    )

    data class NodeTypeTemplateModel(
        val screamingSnakeCaseName: String
    )

    fun compileEnumClass() {
        val mustacheFactory = DefaultMustacheFactory()
        val dataModelMustache: Mustache = mustacheFactory.compile(
            "src/main/resources/templates/GraphQlSimpleNodeType.kt.mustache"
        )

        if (!targetDir.exists()) {
            Files.createDirectories(targetDir)
        }
        val targetFile = targetFilePath.toFile()

        val mappedModelNames = mappedModels
            .map { StringUtils.getCaseSuite(it.simpleName).screamingSnakeCase }
            .distinct()
            .sorted()
        dataModelMustache.execute(
            targetFile.bufferedWriter(),
            TemplateModel(
                mappedModelNames.map { name ->
                    NodeTypeTemplateModel(name)
                }
            ),
        ).flush()

        logger.info("Wrote $targetFilePath")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LangToKtEnumCompiler::class.java)
    }

}
