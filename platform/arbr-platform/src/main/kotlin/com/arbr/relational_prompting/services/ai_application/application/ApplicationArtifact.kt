package com.arbr.relational_prompting.services.ai_application.application

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair

data class ApplicationArtifact(
    val applicationId: String,
    val examples: List<VectorResourceKeyValuePair<*, *>>,
    val input: SourcedStruct,
    val output: SourcedStruct,

    /**
     * Vector IDs for the new published result.
     */
    val vectorIds: List<String>,

    /**
     * Completion usage data
     */
    val usedModel: String,
    val usagePromptTokens: Long,
    val usageCompletionTokens: Long,
    val usageTotalTokens: Long,
)
