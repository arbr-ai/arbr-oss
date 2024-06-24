package com.arbr.codegen.base.json_schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * We don't have access to the kotlin module of Jackson within the extension, so need to be cautious with feature compatibility, for example annotations and final classes
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
open class JsonSchema(
    val type: Any,
    val description: String?,
    val enum: List<String>?,
    val items: JsonSchema?,
    val properties: LinkedHashMap<String, JsonSchema>?,
    val required: List<String>?,
)
