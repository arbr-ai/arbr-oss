package com.arbr.backdraft.target

import com.arbr.graphql_compiler.core.SchemaSource
import java.io.Closeable

class ConverterProcess(
    private val converter: GraphQlConverter,
): Closeable {

    fun convertFromSources(
        schemaSources: List<SchemaSource>
    ) {
        converter.convertFromSources(schemaSources)
    }

    override fun close() {
        // ...
    }
}