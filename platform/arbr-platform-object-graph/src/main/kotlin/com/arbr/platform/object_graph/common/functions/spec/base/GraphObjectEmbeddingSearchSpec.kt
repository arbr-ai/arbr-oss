package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface GraphObjectEmbeddingSearchSpec<RV : ResourceView<*>, U : FunctionInputElement> {

    /**
     * Config parameters for embedding and nearest-neighbors search
     */
    val embeddingSearchConfig: GraphObjectEmbeddingSearchConfig

    /**
     * The input view adapted to the graph object format
     */
    val transformedInput: U

    /**
     * The query spec for sourcing and transforming candidate neighbors
     */
    val querySpec: GraphObjectQuerySpec<RV, U>

}

