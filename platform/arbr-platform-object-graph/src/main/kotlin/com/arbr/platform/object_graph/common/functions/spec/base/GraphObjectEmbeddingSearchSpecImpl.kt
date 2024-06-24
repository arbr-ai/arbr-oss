package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

data class GraphObjectEmbeddingSearchSpecImpl<RV : ResourceView<*>, U : FunctionInputElement>(
    override val embeddingSearchConfig: GraphObjectEmbeddingSearchConfig,
    override val transformedInput: U,
    override val querySpec: GraphObjectQuerySpec<RV, U>,
): GraphObjectEmbeddingSearchSpec<RV, U>