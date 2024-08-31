package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

class RValueFunctionDelegateProvider<RV : ResourceView<*>, T>(
    private val provideByName: (name: String) -> RValueFunction<RV, T>,
) : DelegateProviderImpl<RValueFunction<RV, T>>(
    { provideByName(it.name) }
)
