package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

data class GraphObjectEmbeddingSearchSpecImpl<RV : ResourceView<*>, U : FunctionInputElement>(
    override val embeddingSearchConfig: GraphObjectEmbeddingSearchConfig,
    override val transformedInput: U,
    override val querySpec: GraphObjectQuerySpec<RV, U>,
): GraphObjectEmbeddingSearchSpec<RV, U>