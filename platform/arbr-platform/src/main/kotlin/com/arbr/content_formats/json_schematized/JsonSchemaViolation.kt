package com.arbr.content_formats.json_schematized

/**
 * TODO: Go one level up in the object for context
 */
data class JsonSchemaViolation(
    val localObject: Any?,
    val selector: String,
    val message: String,
)