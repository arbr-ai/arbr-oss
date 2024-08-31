package com.arbr.platform.object_graph.artifact

import com.arbr.platform.object_graph.common.values.collections.SourcedStruct

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
