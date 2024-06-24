package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode

@JsonDeserialize(using = OpenAiCompletionPrompt.Deserializer::class)
sealed class OpenAiCompletionPrompt(
    @JsonValue
    open val value: Any
) {
    class Deserializer: StdDeserializer<OpenAiCompletionPrompt>(OpenAiCompletionPrompt::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OpenAiCompletionPrompt {
            val jsonNode: JsonNode = p.codec.readTree(p)

            return when {
                jsonNode.isTextual -> SingleText(jsonNode.textValue())
                jsonNode.isArray -> (jsonNode as ArrayNode).let { jsonArray ->
                    if (jsonArray.isEmpty) {
                        BatchText(emptyList())
                    } else {
                        val firstValue = jsonArray.first()

                        if (firstValue.isTextual) {
                            BatchText(jsonArray.map { it.textValue() })
                        } else if (firstValue.isInt) {
                            SingleTokenList(jsonArray.map { it.intValue() })
                        } else if (firstValue.isArray) {
                            val value = (firstValue as ArrayNode).map { tokenArray ->
                                (tokenArray as ArrayNode).map { it.intValue() }
                            }
                            BatchTokenList(value)
                        } else {
                            throw Exception("Invalid prompt value encountered during deserialization")
                        }
                    }
                }
                else -> throw Exception("Invalid prompt value encountered during deserialization")
            }
        }
    }

    data class SingleText(
        @JsonValue
        override val value: String
    ): OpenAiCompletionPrompt(value)

    data class BatchText(
        @JsonValue
        override val value: List<String>
    ): OpenAiCompletionPrompt(value)

    data class SingleTokenList(
        @JsonValue
        override val value: List<Int>
    ): OpenAiCompletionPrompt(value)

    data class BatchTokenList(
        @JsonValue
        override val value: List<List<Int>>
    ): OpenAiCompletionPrompt(value)

    companion object {
        @JsonCreator
        @JvmStatic
        fun create(
            value: Any
        ): OpenAiCompletionPrompt {
            return when (value) {
                is String -> SingleText(value)
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    when {
                        value.isEmpty() -> BatchText(emptyList())
                        value.first() is String -> BatchText(value as List<String>)
                        value.first() is Int -> SingleTokenList(value as List<Int>)
                        else -> BatchTokenList(value as List<List<Int>>)
                    }
                }
                else -> throw Exception()
            }
        }
    }
}
