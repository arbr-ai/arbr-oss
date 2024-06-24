package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

data class HeterogeneousGraphObjectQuerySpec<
        RV : ResourceView<*>,
        RVQ : ResourceView<*>,
        U : FunctionInputElement,
        >(
    val inputAdapterQuerySpec: GraphObjectQuerySpec<RV, U>,
    val properQuerySpec: GraphObjectQuerySpec<RVQ, U>,
)
