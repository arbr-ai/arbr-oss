package com.arbr.platform.object_graph.util

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.platform.object_graph.common.ObjectModel

object JsonSchemaUtils {

    private fun mergeJsonSchema(
        vararg objectTypes: ObjectModel.ObjectType<*, *, *, *>
    ): JsonSchema {
        val mergedProperties = linkedMapOf<String, JsonSchema>().also { lhm ->
            objectTypes.forEach {
                val properties = it.jsonSchema.properties
                if (properties != null) {
                    lhm.putAll(properties)
                }
            }
        }

        val required = objectTypes.flatMap { it.jsonSchema.required ?: emptyList() }.distinct()

        return JsonSchema(
            "object",
            description = null,
            enum = null,
            items = null,
            properties = mergedProperties,
            required = required,
        )
    }

    fun mergedArrayJsonSchema(
        description: String,
        vararg objectTypes: ObjectModel.ObjectType<*, *, *, *>
    ): JsonSchema {
        val mergedSchema = mergeJsonSchema(*objectTypes)

        return JsonSchema(
            "array",
            description = description,
            enum = null,
            items = mergedSchema,
            properties = null,
            required = mergedSchema.required,
        )
    }

    /**
     * For the property schema of an object, check that the key is required in the root JsonSchema and the value schema
     * does not allow null.
     */
    fun requires(
        jsonSchema: JsonSchema,
        key: String,
    ): Boolean {
        val propertySchema = jsonSchema.properties?.get(key) ?: return false

        val schemaRequired = jsonSchema.required
        return if (schemaRequired == null || key !in schemaRequired) {
            false
        } else {
            val schemaType = propertySchema.type
            !(schemaType is List<*> && "null" in schemaType)
        }
    }
}