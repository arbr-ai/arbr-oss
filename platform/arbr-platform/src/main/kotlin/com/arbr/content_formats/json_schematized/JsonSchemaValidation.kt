package com.arbr.content_formats.json_schematized

import com.arbr.content_formats.mapper.Mappers
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.fasterxml.jackson.annotation.JsonValue
import org.slf4j.LoggerFactory

object JsonSchemaValidation {
    private val logger = LoggerFactory.getLogger(JsonSchemaValidation::class.java)
    private val mapper = Mappers.mapper

    private enum class JsonType(@JsonValue val serializedName: String) {
        ARRAY("array"),
        OBJECT("object"),
        BOOLEAN("boolean"),
        STRING("string"),
        NUMBER("number"),
        NULL("null");

        companion object {
            fun ofSerializedName(serializedName: String): JsonType? {
                return values().firstOrNull { it.serializedName == serializedName }
            }
        }
    }

    fun detectSchemaViolations(propertySchemaList: List<PropertySchema<*, *>>, parsedMap: LinkedHashMap<String, Any>): List<JsonSchemaViolation> {
        val violations = mutableListOf<JsonSchemaViolation>()

        fun detectInnerSchemaViolation(selector: String, jsonSchema: JsonSchema, obj: Any?) {
            val schemaType = jsonSchema.type
            val validTypes = if (schemaType is List<*>) {
                schemaType.map { it.toString() }
            } else {
                listOf(schemaType.toString())
            }.mapNotNull {
                val jsonType = JsonType.ofSerializedName(it)

                if (jsonType == null) {
                    logger.error("Invalid type in json schema: $it")
                }

                jsonType
            }

            if (obj == null) {
                if (JsonType.NULL in validTypes) {
                    // Valid null
                    return
                } else {
                    violations.add(
                        JsonSchemaViolation(
                            null,
                            selector,
                            "Value was unexpectedly null."
                        )
                    )
                    return
                }
            }

            if (obj is List<*>) {
                if (!validTypes.contains(JsonType.ARRAY)) {
                    // Fail: invalid object
                    val typeMessage = if (validTypes.size == 1) {
                        "Expected value type of ${validTypes}. Got \"array\"."
                    } else {
                        "Expected value type in ${mapper.writeValueAsString(validTypes)}. Got \"array\"."
                    }
                    violations.add(
                        JsonSchemaViolation(
                            obj,
                            selector,
                            typeMessage,
                        )
                    )
                    return
                }

                if (jsonSchema.items == null) {
                    // Schema error
                    logger.error("Items schema for array missing on $selector")
                }

                jsonSchema.items?.let { innerSchema ->
                    obj.forEachIndexed { index, innerObject ->
                        val innerSelector = "$selector[$index]"
                        detectInnerSchemaViolation(innerSelector, innerSchema, innerObject)
                    }
                }

                return
            }

            if (obj is Map<*, *>) {
                if (!validTypes.contains(JsonType.OBJECT)) {
                    // Fail: invalid object
                    val typeMessage = if (validTypes.size == 1) {
                        "Expected value type of ${validTypes}. Got \"object\"."
                    } else {
                        "Expected value type in ${mapper.writeValueAsString(validTypes)}. Got \"object\"."
                    }
                    violations.add(
                        JsonSchemaViolation(
                            obj,
                            selector,
                            typeMessage,
                        )
                    )
                    return
                }

                if (jsonSchema.properties == null) {
                    // Schema error
                    logger.error("Schema for object missing properties on $selector")
                }

                jsonSchema.properties?.forEach { (innerKey, innerSchema) ->
                    val innerSelector = "$selector.$innerKey"

                    if (jsonSchema.required?.contains(innerKey) == true && obj.containsKey(innerKey)) {
                        detectInnerSchemaViolation(innerSelector, innerSchema, obj[innerKey]!!)
                    } else {
                        violations.add(
                            JsonSchemaViolation(
                                obj,
                                selector,
                                "Object missing required key: $innerKey",
                            )
                        )
                    }
                }

                return
            }

            if (validTypes.contains(JsonType.STRING) || validTypes.contains(JsonType.NUMBER) || validTypes.contains(
                    JsonType.BOOLEAN
                )) {
                // Pass: value type
                return
            }

            logger.info("Value type not valid for $selector despite value: $obj")
        }

        for (p in propertySchemaList) {
            val (key, jsonSchema) = p.property()
            if (jsonSchema.required?.contains(key) == true && !parsedMap.containsKey(key)) {
                logger.info("Schema validation: Missing root key: $key")
            }

            jsonSchema.properties?.forEach { (innerKey, innerSchema) ->
                val selector = "$key.$innerKey"
                detectInnerSchemaViolation(selector, innerSchema, parsedMap[key]!!)
            }
            jsonSchema.items?.let { innerSchema ->
                (parsedMap[key]!! as List<*>).forEachIndexed { i, obj ->
                    val selector = "$key[$i]"
                    detectInnerSchemaViolation(selector, innerSchema, obj)
                }
            }

        }
        
        return violations
    }
}
