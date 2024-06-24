package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.content_formats.mapper.Mappers
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.relational_prompting.layers.object_translation.TemplateDescriptionElement
import com.arbr.relational_prompting.layers.object_translation.TemplateValueElement

object YamlPropertyListSerializer: PropertyListSerializer {
    private val yamlMapper = Mappers.yamlMapper

    enum class ValueKind {
        VALUE, ARRAY, OBJECT
    }

    private fun renderYaml(schema: JsonSchema): Pair<ValueKind, String> {
        val schemaProperties = schema.properties
        return if (schemaProperties == null && schema.items == null) {
            // Value
            val typeString = when (val schemaType = schema.type) {
                is List<*> -> schemaType[0].toString()
                is Array<*> -> schemaType[0].toString()
                else -> schemaType.toString()
            }

            ValueKind.VALUE to "# ($typeString) ${schema.description ?: ""}"
        } else if (schemaProperties == null) {
            // Array
            val (_, innerYaml) = renderYaml(schema.items!!)
            val paddedInnerYaml = innerYaml
                .split("\n")
                .withIndex()
                .joinToString("\n") { (i, s) ->
                    if (i > 0) {
                        "  $s"
                    } else {
                        s
                    }
                }
            ValueKind.ARRAY to "- $paddedInnerYaml\n- # ..."
        } else {
            // Object
            ValueKind.OBJECT to schemaProperties.map { (name, innerSchema) ->
                val (kind, innerYaml) = renderYaml(innerSchema)
                when (kind) {
                    ValueKind.VALUE -> "$name: $innerYaml"
                    ValueKind.ARRAY,
                    ValueKind.OBJECT -> {
                        val paddedInnerYaml = innerYaml.split("\n")
                            .joinToString("\n") { "  $it" }
                        "$name: # ${innerSchema.description ?: ""}\n$paddedInnerYaml"
                    }
                }
            }.joinToString("\n")
        }
    }

    private fun wrapYaml(innerRenderedYaml: String): String {
        return if (innerRenderedYaml.startsWith("```")) {
            innerRenderedYaml
        } else {
            return "```yaml\n${innerRenderedYaml.trim()}\n```"
        }
    }

    private fun describe(jsonSchema: JsonSchema): String {
        val (_, rendered) = renderYaml(jsonSchema)
        return wrapYaml(rendered)
    }

    override fun describeProperties(propertySchemas: List<PropertySchema<*, *>>): TemplateDescriptionElement {
        val mergedProperties = LinkedHashMap<String, JsonSchema>().also { lhm ->
            propertySchemas.forEach { ps ->
                val (key, prop) = ps.property()
                if (key in lhm) {
                    throw TemplateComponentSchema.DuplicatePropertyKeyException(key)
                }

                lhm[key] = prop
            }
        }
        val combinedJsonSchema = JsonSchema(
            type = "object",
            description = null,
            enum = null,
            items = null,
            properties = mergedProperties,
            required = mergedProperties.keys.toList(),
        )

        return TemplateDescriptionElement(
            describe(combinedJsonSchema)
        )
    }

    override fun serializeValuedProperties(
        propertySchemas: List<PropertySchema<*, *>>,
        unwrappedPropertyValues: List<*>,
    ): TemplateValueElement {
        if (propertySchemas.size != unwrappedPropertyValues.size) {
            throw Exception("Value length mismatch in property values")
        }

        val valueMap = LinkedHashMap<String, Any?>()
        propertySchemas.zip(unwrappedPropertyValues).forEach { (propertySchema, value) ->
            val (key, _) = propertySchema.property()
            valueMap[key] = value
        }

        return TemplateValueElement(
            yamlMapper.writeValueAsString(valueMap).trimEnd()
                .let { "```yaml\n$it\n```" }
        )
    }

}