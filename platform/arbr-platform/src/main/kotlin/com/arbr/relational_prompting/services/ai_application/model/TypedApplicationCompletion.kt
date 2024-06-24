package com.arbr.relational_prompting.services.ai_application.model

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.generics.model.ChatMessage

data class TypedApplicationCompletion<InputModel : SourcedStruct, OutputModel : SourcedStruct>(
    val cacheKey: String,
    val creationTimestamp: Long,
    val applicationId: String,
    val exampleVectorIds: List<String>,
    val inputVectorIds: List<String>,
    val inputResource: InputModel,
    val outputResource: OutputModel,
    val promptMessages: List<ChatMessage>,
    val completionMessages: List<ChatMessage>,
    val usedModel: String,
    val promptTokens: Long,
    val completionTokens: Long,
    val totalTokens: Long,
)