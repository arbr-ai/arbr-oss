package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

//fun interface InputPropertySerializer<V> {
//    fun serialize(value: V): String
//}

interface ResourceHelperFunctionInputSpecConfigurer<RV : ResourceView<*>> {
    fun <I : FunctionInputElement> inputting(
        inputClass: Class<I>,
        configure: GraphObjectQueryMapContext.(RV) -> I
    ): RValueFunctionDelegateProvider<RV, I>
}

inline fun <RV : ResourceView<*>, reified I : FunctionInputElement> ResourceHelperFunctionInputSpecConfigurer<RV>.inputting(
    noinline configure: GraphObjectQueryMapContext.(RV) -> I,
): RValueFunctionDelegateProvider<RV, I> {
    return inputting(I::class.java, configure)
}

