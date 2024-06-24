package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode

@JsonDeserialize(using = OpenAiChatCompletionStop.Deserializer::class)
sealed class OpenAiChatCompletionStop(
    @JsonValue
    open val value: Any
) {

    class Deserializer: StdDeserializer<OpenAiChatCompletionStop>(OpenAiChatCompletionStop::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OpenAiChatCompletionStop {
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
    ): OpenAiChatCompletionStop(value)

    data class MultiText(
        @JsonValue
        override val value: List<String>
    ): OpenAiChatCompletionStop(value)

    companion object {
        @JsonCreator
        @JvmStatic
        fun create(
            value: Any
        ): OpenAiChatCompletionStop {
            @Suppress("UNCHECKED_CAST")
            return when (value) {
                is String -> SingleText(value)
                is List<*> -> MultiText(value as List<String>)
                else -> throw Exception()
            }
        }
    }
}
