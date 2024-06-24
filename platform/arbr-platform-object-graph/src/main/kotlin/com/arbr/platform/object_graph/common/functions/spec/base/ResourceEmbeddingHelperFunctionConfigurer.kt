package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface ResourceEmbeddingHelperFunctionConfigurer<RV : ResourceView<*>> : ResourceHelperFunctionConfigurer<RV>,
    ResourceHelperFunctionInputSpecConfigurer<RV>, ResourceHelperFunctionReturnSpecConfigurer<RV> {
    fun configuring(configure: ResourceEmbeddingHelperFunctionConfigContext.() -> Unit): DelegateProvider<GraphObjectEmbeddingSearchConfig>

    /**
     * Query through a resource view of a potentially different type.
     * TODO: Support conveniently with the builder
     */
    fun <
            RVQ: ResourceView<*>,
            T : FunctionInputElement
            > querying(
        queryResourceViewClass: Class<RVQ>,
        configure: ResourceEmbeddingHelperFunctionQuerySpecContext.() -> HeterogeneousGraphObjectQuerySpec<RV, RVQ, T>
    ): RValueFunctionDelegateProvider<RV, GraphObjectEmbeddingSearchSpec<RVQ, T>>

    /**
     * Query through a resource view of the same type.
     */
    fun <
            T : FunctionInputElement
            > querying(
        configure: ResourceEmbeddingHelperFunctionQuerySpecContext.() -> GraphObjectQuerySpec<RV, T>
    ): RValueFunctionDelegateProvider<RV, GraphObjectEmbeddingSearchSpec<RV, T>> {
        return querying(
            resourceViewClass,
        ) {
            val spec = configure(this)
            HeterogeneousGraphObjectQuerySpec(spec, spec)
        }
    }
}
