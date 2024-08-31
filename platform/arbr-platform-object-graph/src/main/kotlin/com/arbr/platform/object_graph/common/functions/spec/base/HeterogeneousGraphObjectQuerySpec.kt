package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

data class HeterogeneousGraphObjectQuerySpec<
        RV : ResourceView<*>,
        RVQ : ResourceView<*>,
        U : FunctionInputElement,
        >(
    val inputAdapterQuerySpec: GraphObjectQuerySpec<RV, U>,
    val properQuerySpec: GraphObjectQuerySpec<RVQ, U>,
)
