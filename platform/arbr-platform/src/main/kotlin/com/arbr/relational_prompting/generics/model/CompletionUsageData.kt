package com.arbr.relational_prompting.generics.model

data class CompletionUsageData(
    val usedModel: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
)