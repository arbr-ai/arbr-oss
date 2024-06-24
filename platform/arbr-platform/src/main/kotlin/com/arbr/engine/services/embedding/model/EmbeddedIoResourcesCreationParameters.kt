package com.arbr.engine.services.embedding.model

import com.arbr.relational_prompting.services.ai_application.config.ResourceEmbeddingContent
import com.arbr.db.public.tables.records.IndexedResourceRecord

data class EmbeddedResourcePairCreationParameters(
    val inputRecord: IndexedResourceRecord,
    val outputRecord: IndexedResourceRecord,
    val inputEmbeddingContents: List<ResourceEmbeddingContent>,
)