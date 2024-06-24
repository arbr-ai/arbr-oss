package com.arbr.graphql.lang.common

import com.arbr.graphql.lang.node.GraphQlLanguageNodeTraversalState
import java.io.Closeable
import java.io.Writer

interface GraphQlNodeProcessor: Closeable {

    val writer: Writer

    fun processNode(
        traversalState: GraphQlLanguageNodeTraversalState,
    )

    override fun close() {
        writer.flush()
        writer.close()
    }

}
