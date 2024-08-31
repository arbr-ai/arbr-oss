package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.common.properties.FieldValueViewContainer
import com.arbr.platform.object_graph.common.functions.platform.ResourceFunctionContext


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
