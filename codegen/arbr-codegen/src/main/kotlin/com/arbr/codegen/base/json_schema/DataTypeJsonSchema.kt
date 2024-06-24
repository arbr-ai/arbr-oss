package com.arbr.codegen.base.json_schema

import com.arbr.codegen.base.inputs.ArbrPrimitiveValueType

private fun <K, V> linkedHashMapOf(pairList: List<Pair<K , V>>): LinkedHashMap<K, V> {
    return LinkedHashMap<K, V>().also {
        pairList.forEach { (key, value) ->
            it[key] = value
        }
    }
}

sealed class DataTypeJsonSchema {
    abstract fun toProperty(): JsonSchema

    class Builder {
        private val schemaList = mutableListOf<Pair<String, DataTypeJsonSchema>>()

        fun value(field: DataTypeJsonField) {
            schemaList.add(field.name to DataTypeJsonSchemaValue(field))
        }

        fun literalValue(name: String, jsonSchema: JsonSchema) {
            schemaList.add(name to DataTypeJsonSchemaLiteralValue(jsonSchema))
        }

        fun list(
            name: String,
            description: String,
            field: DataTypeJsonField
        ) {
            schemaList.add(name to DataTypeJsonSchemaList(description, field))
        }

        fun literalList(
            name: String,
            description: String?,
            jsonSchema: JsonSchema,
        ) {
            schemaList.add(name to DataTypeJsonSchemaLiteralList(description, jsonSchema))
        }

        fun nestedObjectList(
            name: String,
            description: String,
            block: Builder.() -> Unit,
        ) {
            schemaList.add(name to DataTypeJsonSchemaLiteralList(description, build(block)))
        }

        fun obj(
            name: String,
            description: String,
            vararg fields: DataTypeJsonField
        ) {
            schemaList.add(name to DataTypeJsonSchemaObject(
                description,
                fields.toList()
            )
            )
        }

        val properties: List<Pair<String, JsonSchema>>
            get() = schemaList.map { it.first to it.second.toProperty() }
    }

    companion object {
        fun build(
            block: Builder.() -> Unit,
        ): JsonSchema {
            val builder = Builder()
            block(builder)

            return JsonSchema(
                type = "object",
                description = null,
                enum = null,
                items = null,
                properties = linkedHashMapOf(builder.properties),
                required = builder.properties.map { it.first },
            )
        }
    }
}

class DataTypeJsonSchemaLiteralValue(private val jsonSchema: JsonSchema): DataTypeJsonSchema() {
    override fun toProperty(): JsonSchema {
        return jsonSchema
    }
}

/**
 * {
 *   "type": ["string", "null"],
 *   "description": "",
 * }
 */
class DataTypeJsonSchemaValue(private val field: DataTypeJsonField): DataTypeJsonSchema() {

    override fun toProperty(): JsonSchema {

        val type = when (field.valueType) {
            ArbrPrimitiveValueType.BOOLEAN -> "boolean"
            ArbrPrimitiveValueType.INTEGER,
            ArbrPrimitiveValueType.LONG,
            ArbrPrimitiveValueType.FLOAT,
            ArbrPrimitiveValueType.DOUBLE -> "number"
            ArbrPrimitiveValueType.STRING -> "string"
        }

        val nullType = if (field.required) null else "null"

        return JsonSchema(
            type = listOfNotNull(type, nullType),
            description = field.description,
            enum = null,
            items = null,
            properties = null,
            required = null,
        )
    }
}

/**
 * {
 *   "type": "array",
 *   "items": {
 *     "type": "number"
 *   }
 * }
 */
class DataTypeJsonSchemaList(
    private val description: String,
    private val itemField: DataTypeJsonField,
): DataTypeJsonSchema() {
    override fun toProperty(): JsonSchema {
        val innerSchemaProperty = DataTypeJsonSchemaValue(itemField).toProperty()

        return JsonSchema(
            type = "array",
            description = description,
            enum = null,
            items = innerSchemaProperty,
            properties = null,
            required = null,
        )
    }
}


class DataTypeJsonSchemaLiteralList(
    private val description: String?,
    val property: JsonSchema,
): DataTypeJsonSchema() {
    override fun toProperty(): JsonSchema {
        return JsonSchema(
            type = "array",
            description = description,
            enum = null,
            items = property,
            properties = null,
            required = null,
        )
    }
}

/**
 * {
 *   "type": "object",
 *   "properties": {
 *     "number": { "type": "number" },
 *     "street_name": { "type": "string" },
 *     "street_type": { "enum": ["Street", "Avenue", "Boulevard"] }
 *   }
 * }
 */
class DataTypeJsonSchemaObject(
    private val description: String,
    private val fields: List<DataTypeJsonField>
): DataTypeJsonSchema() {
    override fun toProperty(): JsonSchema {
        val properties = fields.map { field ->
            val innerSchemaProperty = DataTypeJsonSchemaValue(field).toProperty()
            field.name to innerSchemaProperty
        }

        return JsonSchema(
            type = "object",
            description = description,
            enum = null,
            items = null,
            properties = linkedHashMapOf(properties),
            required = properties.map { it.first },
        )
    }
}
