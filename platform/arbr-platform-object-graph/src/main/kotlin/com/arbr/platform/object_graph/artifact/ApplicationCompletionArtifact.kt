package com.arbr.og_engine.artifact

import com.arbr.og.object_model.common.values.collections.SourcedStruct

/**
 * Artifact for a text completion from an AiApplication
 */
data class ApplicationCompletionArtifact(
    val applicationId: String,
    val exampleObjects: List<Map<String, Any>>,
    val input: SourcedStruct,
    val output: SourcedStruct,

    /**
     * Vector IDs for the new published result.
     */
    val vectorIds: List<String>,
): Artifact
