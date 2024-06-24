package com.arbr.backdraft.target

import com.arbr.graphql.lang.common.CompilerProcessNodeVisitor
import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import graphql.language.NodeVisitor
import java.io.Closeable

class CompilationProcess(
    private val nodeProcessor: GraphQlNodeProcessor,
) : NodeVisitor by CompilerProcessNodeVisitor(nodeProcessor), Closeable {
    override fun close() {
        nodeProcessor.close()
    }
}
