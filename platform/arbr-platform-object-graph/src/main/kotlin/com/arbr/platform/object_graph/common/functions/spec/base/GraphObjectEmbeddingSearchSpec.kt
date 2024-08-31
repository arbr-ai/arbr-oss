package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

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

