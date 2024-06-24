package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

class RValueFunctionDelegateProvider<RV : ResourceView<*>, T>(
    private val provideByName: (name: String) -> RValueFunction<RV, T>,
) : DelegateProviderImpl<RValueFunction<RV, T>>(
    { provideByName(it.name) }
)
