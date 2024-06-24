package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.content_formats.json_schematized.JsonSchemaViolation

class OutputFormatException(outputContent: String, cause: Throwable): Exception(outputContent, cause)

/**
 * Output is valid but does not match expected schema.
 */
class OutputSchemaException(val parsedMap: LinkedHashMap<String, Any>, override val cause: Throwable): Exception(parsedMap.toString(), cause)

class OutputSchemaExceptionWithKnownViolations(
    val messageContent: String,
    val parsedMap: LinkedHashMap<String, Any>,
    val violations: List<JsonSchemaViolation>,
    override val cause: Throwable
): Exception(parsedMap.toString(), cause)
