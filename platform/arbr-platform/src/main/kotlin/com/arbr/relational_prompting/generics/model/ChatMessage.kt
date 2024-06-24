package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChatMessage(
    val role: Role,
    val content: String,
    val name: String? = null,
    val functionCall: ChatMessageFunctionCall? = null,
) {

    enum class Role(@JsonValue val roleName: String) {
        SYSTEM("system"),
        ASSISTANT("assistant"),
        USER("user"),
    }

    companion object {
        @JsonCreator
        @JvmStatic
        fun create(
            role: Role,
            content: String?,
            name: String?,
            functionCall: ChatMessageFunctionCall?,
        ): ChatMessage {
            return ChatMessage(
                role,
                content ?: "",
                name,
                functionCall,
            )
        }
    }
}
