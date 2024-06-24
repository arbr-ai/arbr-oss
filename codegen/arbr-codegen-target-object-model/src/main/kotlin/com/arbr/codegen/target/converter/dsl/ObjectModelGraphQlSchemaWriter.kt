package com.arbr.codegen.target.converter.dsl

import com.arbr.graphql_compiler.model.graphql.GraphQlSchemaDocumentModel
import com.github.mustachejava.DefaultMustacheFactory
import java.io.File
import java.io.Writer
import java.nio.file.Path
import java.nio.file.Paths

class ObjectModelGraphQlSchemaWriter(
    val targetFile: File,
    templateFilePathString: String,
) {
    private val mustacheFactory = DefaultMustacheFactory()
    private val mustache = mustacheFactory.compile(templateFilePathString)

    private var writer: Writer? = null

    private fun currentWriter(): Writer {
        val writerVar = writer
        if (writerVar != null) {
            return writerVar
        }

        if (targetFile.exists()) {
            targetFile.delete()
        }
        targetFile.createNewFile()
        return targetFile.bufferedWriter().also {
            this.writer = it
        }
    }

    fun write(
        schemaDocumentModel: GraphQlSchemaDocumentModel,
        headerLines: List<String> = emptyList(),
    ): Writer {
        val currentWriter = currentWriter()

        headerLines.forEach(currentWriter::appendLine)
        currentWriter.flush()
        currentWriter.close()

        val nextWriter = targetFile.bufferedWriter()
        val newWriter = mustache.execute(
            nextWriter,
            schemaDocumentModel
        )

        this.writer = newWriter
        newWriter.flush()
        return newWriter
    }

    companion object {
        fun defaultOutputFileInDir(
            outputDirPath: Path
        ): ObjectModelGraphQlSchemaWriter {
            val targetFile = Paths.get(outputDirPath.toString(), "object_model.graphql").toFile()
            val templateFilePath = "templates/graphql/object_model.graphql.mustache"
            return ObjectModelGraphQlSchemaWriter(targetFile, templateFilePath)
        }
    }
}