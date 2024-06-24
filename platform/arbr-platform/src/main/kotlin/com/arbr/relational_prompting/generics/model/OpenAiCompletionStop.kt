package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode

@JsonDeserialize(using = OpenAiCompletionStop.Deserializer::class)
sealed class OpenAiCompletionStop(
    @JsonValue
    open val value: Any
) {

    class Deserializer: StdDeserializer<OpenAiCompletionStop>(OpenAiCompletionStop::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OpenAiCompletionStop {
            val jsonNode: JsonNode = p.codec.readTree(p)

            return when {
                jsonNode.isTextual -> SingleText(jsonNode.textValue())
                jsonNode.isArray -> MultiText((jsonNode as ArrayNode).map { it.textValue() })
                else -> throw Exception("Invalid prompt value encountered during deserialization")
            }
        }
    }

    data class SingleText(
        @JsonValue
        override val value: String
    ): OpenAiCompletionStop(value)

    data class MultiText(
        @JsonValue
        override val value: List<String>
    ): OpenAiCompletionStop(value)

    companion object {
        @JsonCreator
        @JvmStatic
        fun create(
            value: Any
        ): OpenAiCompletionStop {
            @Suppress("UNCHECKED_CAST")
            return when (value) {
                is String -> SingleText(value)
                is List<*> -> MultiText(value as List<String>)
                else -> throw Exception()
            }
        }
    }
}
