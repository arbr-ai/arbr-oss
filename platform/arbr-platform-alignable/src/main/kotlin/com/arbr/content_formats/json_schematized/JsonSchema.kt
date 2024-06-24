package com.arbr.content_formats.json_schematized

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class JsonSchema(
    val type: Any,
    val description: String?,
    val enum: List<String>?,
    val items: JsonSchema?,
    val properties: LinkedHashMap<String, JsonSchema>?,
    val required: List<String>?,
) {

    @JsonIgnore
    fun requiredNonNull(): JsonSchema {
        val propertyKeys = properties?.keys?.toList()
        val nextType = if (type is List<*>) {
            type.filter { it != "null" }
        } else {
            type
        }
        return copy(type=nextType, required = propertyKeys)
    }
}
