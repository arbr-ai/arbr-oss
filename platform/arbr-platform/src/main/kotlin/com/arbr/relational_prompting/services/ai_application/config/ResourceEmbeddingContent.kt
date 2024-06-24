package com.arbr.relational_prompting.services.ai_application.config

data class ResourceEmbeddingContent(
    val schemaId: String,
    val kind: ResourceEmbeddingKind,
    val content: String,
)