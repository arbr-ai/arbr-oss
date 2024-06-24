package com.arbr.og.object_model.common.functions.spec.base

data class GraphObjectEmbeddingSearchConfig(
    var model: String,
    var similarityThreshold: Double,
    var maxResults: Int,
    var includeMetadata: Boolean,
    var namespace: String?,
)

interface ResourceEmbeddingHelperFunctionConfigContext {
    var model: String
    var similarityThreshold: Double
    var maxResults: Int
    var includeMetadata: Boolean
    var namespace: String?
}

/**
 * TODO: Extract defaults, source from configuration, and expand options
 */
internal class ResourceEmbeddingHelperFunctionConfigContextImpl: ResourceEmbeddingHelperFunctionConfigContext {
    override var model: String = ""
    override var similarityThreshold: Double = 0.8
        set(value) {
            field = value.coerceIn(0.0, 1.0)
        }
    override var maxResults: Int = 10
        set(value) {
            field = value.coerceAtLeast(1)
        }
    override var includeMetadata: Boolean = false
    override var namespace: String? = null

    /**
     * Render to immutable config.
     */
    fun toConfig(): GraphObjectEmbeddingSearchConfig {
        return GraphObjectEmbeddingSearchConfig(
            model,
            similarityThreshold,
            maxResults,
            includeMetadata,
            namespace,
        )
    }
}
