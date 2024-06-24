package com.arbr.graphql_compiler.core

import java.io.File
import java.util.stream.Stream

open class SchemaFileCollectionSource(
    fileStream: Stream<File>,
): SchemaUriCollectionSource(
    fileStream.map { it.toURI() }
)