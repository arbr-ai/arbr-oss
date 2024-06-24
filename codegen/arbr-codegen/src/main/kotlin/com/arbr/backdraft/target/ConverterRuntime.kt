package com.arbr.backdraft.target

import com.arbr.backdraft.task.result.TaskWorkResult
import com.arbr.graphql.lang.common.GraphQlLanguageNodeTransformer
import com.arbr.graphql.lang.node.GraphQlLanguageNodeDocument
import com.arbr.graphql.lang.parser.SchemaDocumentLoader
import com.arbr.graphql_compiler.core.SchemaFileCollectionSource
import com.arbr.graphql_compiler.core.SchemaSource
import org.apache.commons.io.file.PathUtils
import org.apache.commons.io.filefilter.FileFileFilter
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

class ConverterRuntime(
    private val converterTarget: ConverterTarget,
) {
    private fun convertWithProcess(
        sources: List<SchemaSource>,
        converterProcess: ConverterProcess,
    ) {
        return converterProcess.convertFromSources(sources)
    }

    fun convertAgainstTarget(
        sources: List<SchemaSource>
    ) {
        val process = try {
            converterTarget
                .initialize()
        } catch (e: Exception) {
            logger.error("Failed to initialize converter process", e)
            throw e
        }

        process.use {
            convertWithProcess(sources, it)
        }
    }

    private fun listFilesRecursively(
        dirPath: Path,
    ): Stream<Path> {
        val pathFilter = FileFileFilter.INSTANCE
        val maxDepth = Int.MAX_VALUE

        return PathUtils.walk(dirPath, pathFilter, maxDepth, false)
    }

    fun getOutputDocument(): GraphQlLanguageNodeDocument {
        val path = converterTarget.getBuildOutputDir() ?: throw IllegalStateException()
        val fileStream = listFilesRecursively(path)
            .map { it.toFile() }
            .filter {
                it.extension in validExtensions
            }

        val document = SchemaDocumentLoader().loadDocuments(
            listOf(
                SchemaFileCollectionSource(
                    fileStream
                )
            )
        )
        return GraphQlLanguageNodeTransformer().convertDocument(document)
    }

    fun getWorkResult(): TaskWorkResult {
        // TODO: Implement result reporting
        return object : TaskWorkResult {
            override val typeId: String
                get() = converterTarget::class.java.canonicalName
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConverterRuntime::class.java)
        private val validExtensions = setOf("graphql", "graphqls")
    }
}