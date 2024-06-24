package com.arbr.engine.services.embedding.model

import com.arbr.relational_prompting.services.ai_application.config.ResourceEmbeddingContent
import com.arbr.db.public.tables.pojos.VectorEmbedding

/**
 * Newly inserted core embedding.
 * TODO: Deduplicate contents values.
 */
data class InsertedCoreEmbedding(
    val inputContent: ResourceEmbeddingContent,
    val coreEmbedding: VectorEmbedding,
)