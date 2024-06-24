package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.properties.FieldValueViewContainer
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext


interface RFunctionSpecifier<RV : ResourceView<*>> {

    fun <T : FunctionInputElement> helper(
        f: (ResourceHelperFunctionConfigurer<RV>.() -> RValueFunction<RV, T>),
    ): RValueFunctionConfigurableDelegateProvider<RV, T>

    fun <
            T : FunctionInputElement,
            RVQ: ResourceView<*>
            > embedding(
        f: (ResourceEmbeddingHelperFunctionConfigurer<RV>.() -> RValueFunction<RV, GraphObjectEmbeddingSearchSpec<RVQ, T>>),
    ): RValueFunctionConfigurableDelegateProvider<RV, FieldValueViewContainer<RVQ, RVQ, *>>

    fun <T : FunctionInputElement> completion(
        f: (ResourceCompletionHelperFunctionConfigurer<RV>.() -> RValueFunction<RV, T>),
    ): RValueFunctionConfigurableDelegateProvider<RV, T>

    fun mutating(
        mutate: (ResourceFunctionContext.(RV) -> Unit),
    ): RValueFunctionConfigurableDelegateProvider<RV, Unit>
}
