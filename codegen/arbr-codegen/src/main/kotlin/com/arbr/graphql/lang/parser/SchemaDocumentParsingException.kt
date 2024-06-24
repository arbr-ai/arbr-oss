package com.arbr.graphql.lang.parser

import graphql.parser.InvalidSyntaxException
import java.io.Reader

class SchemaDocumentParsingException(
    schemaReader: Reader,
    invalidSyntaxException: InvalidSyntaxException
) : RuntimeException(buildMessage(schemaReader, invalidSyntaxException), invalidSyntaxException) {
    companion object {
        private fun buildMessage(
            schemaReader: Reader,
            invalidSyntaxException: InvalidSyntaxException
        ): String {
            schemaReader.use { reader ->
                return """
                |Unable to parse the schema...
                |${invalidSyntaxException.message}
                | 
                |Schema Section:
                |>>>
                |${invalidSyntaxException.sourcePreview}
                |<<<
                |
                |Full Schema:
                |${reader.readText()}
                """.trimMargin()
            }
        }
    }
}