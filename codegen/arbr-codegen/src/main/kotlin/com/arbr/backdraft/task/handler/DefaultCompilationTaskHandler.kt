package com.arbr.backdraft.task.handler

import com.arbr.backdraft.target.CompilerRuntime
import com.arbr.backdraft.target.CompilerTarget
import com.arbr.backdraft.task.result.TaskWorkResult
import com.arbr.graphql.lang.parser.SchemaDocumentLoader
import com.arbr.graphql_compiler.core.SchemaSource

class DefaultCompilationTaskHandler(
    private val schemaSources: List<SchemaSource>,
) : CompilationTaskHandler {
    override fun handleCompilationTask(target: CompilerTarget): TaskWorkResult {
        val schemaDocumentLoader = SchemaDocumentLoader()
        val document = schemaDocumentLoader.loadDocuments(schemaSources)
        val runtime = CompilerRuntime(target)
        runtime.compileAgainstTarget(document)
        return runtime.getWorkResult()
    }
}