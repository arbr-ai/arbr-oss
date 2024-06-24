package com.arbr.relational_prompting.layers.object_translation

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.platform.object_graph.common.ObjectModel
import com.fasterxml.jackson.annotation.JsonIgnore
import com.arbr.relational_prompting.services.ai_application.config.ResourceEmbeddingKind

data class PropertySchema<ValueType, SourcedValueType : ObjectModel.ObjectValue<ValueType, *, *, SourcedValueType>>(
    val objectType: ObjectModel.ObjectType<ValueType, *, *, SourcedValueType>,

    /**
     * JSON Schema for description and value serialization. Might be altered from its form within `objectType`
     */
    val rootedJsonSchema: JsonSchema,

    /**
     * The kind of embedded resource associated with the property, if any.
     */
    val embeddingKind: ResourceEmbeddingKind?,
) {
    class MalformattedPropertySchemaException: Exception("Property schema not rooted single value object")

    @JsonIgnore
    fun property(): Pair<String, JsonSchema> {
        // Enforce single property
        val entries = rootedJsonSchema.properties!!.entries
        if (entries.size != 1) {
            throw MalformattedPropertySchemaException()
        }

        val (key, value) = entries.first()
        return key to value
    }
}
