package com.arbr.graphql_compiler.core

import java.nio.file.Path
import java.util.stream.Stream

interface SchemaSource {

    fun getSchemaPaths(): Stream<Path>
}

