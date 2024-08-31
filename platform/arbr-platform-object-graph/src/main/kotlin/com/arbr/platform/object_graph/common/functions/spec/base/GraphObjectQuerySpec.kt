package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

interface GraphObjectQuerySpec<RV : ResourceView<*>, U : FunctionInputElement> :
    GraphObjectQueryPivotSpec<RV> {

    /**
     * The transform (& filter) portion of a QuerySpec.
     * There is an implicit filter in the imperative assertion-style verification of requirements that happens in
     * the contextualized block.
     *
     * If the input passes the filters, the result is a detached subgraph as a `FunctionInputElement`.
     */
    val transform: (RV) -> U
}
