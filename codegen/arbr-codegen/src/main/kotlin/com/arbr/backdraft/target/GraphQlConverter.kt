package com.arbr.backdraft.target

import com.arbr.graphql_compiler.core.SchemaSource

fun interface GraphQlConverter {

    fun convertFromSources(
        schemaSources: List<SchemaSource>
    ) // : List<GraphQlLanguageNodeDocument>

}