package com.arbr.backdraft.target

import com.arbr.backdraft.task.result.TaskWorkResult
import graphql.language.Document
import graphql.language.NodeTraverser
import org.slf4j.LoggerFactory

class CompilerRuntime(
    private val compilerTarget: CompilerTarget,
) {
    private fun compileWithProcess(
        document: Document,
        compilationProcess: CompilationProcess,
    ) {
        NodeTraverser().depthFirst(compilationProcess, document)
    }

    fun compileAgainstTarget(
        document: Document
    ) {
        val process = try {
            compilerTarget
                .initialize()
        } catch (e: Exception) {
            logger.error("Failed to initialize compilation process", e)
            throw e
        }

        process.use {
            compileWithProcess(document, it)
        }
    }

    fun getWorkResult(): TaskWorkResult {
        // TODO: Implement result reporting
        return object : TaskWorkResult {
            override val typeId: String
                get() = compilerTarget::class.java.canonicalName
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CompilerRuntime::class.java)
    }
}