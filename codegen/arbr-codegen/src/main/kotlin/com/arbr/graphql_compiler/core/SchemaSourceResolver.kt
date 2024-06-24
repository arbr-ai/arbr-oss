package com.arbr.graphql_compiler.core

import java.net.URI
import java.nio.file.Paths

class SchemaSourceResolver {

    fun loadFiles(schemaFilePathLiterals: List<String>): SchemaSource {
        return SchemaFileCollectionSource(schemaFilePathLiterals.stream().map {
            Paths.get(it).toFile()
        })
    }

    fun loadUris(schemaUriLiterals: List<String>): SchemaSource {
        return SchemaUriCollectionSource(schemaUriLiterals.stream().map {
            URI(it)
        })
    }

}
