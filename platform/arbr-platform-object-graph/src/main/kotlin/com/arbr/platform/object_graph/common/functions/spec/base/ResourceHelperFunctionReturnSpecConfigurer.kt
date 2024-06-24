package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface ResourceHelperFunctionReturnSpecConfigurer<RV : ResourceView<*>> {
    fun <T : FunctionInputElement> returning(
        returnClass: Class<T>,
        configure: ResourceHelperFunctionInputSpecContext.(RV) -> T
    ): RValueFunction<RV, T>
}

inline fun <RV : ResourceView<*>, reified T : FunctionInputElement> ResourceHelperFunctionReturnSpecConfigurer<RV>.returning(
    noinline configure: ResourceHelperFunctionInputSpecContext.(RV) -> T,
): RValueFunction<RV, T> {
    return returning(T::class.java, configure)
}

class BaseResourceHelperFunctionReturnSpecConfigurer<RV : ResourceView<*>>(
    private val resourceViewClass: Class<RV>,
    contextName: String,
) : ResourceHelperFunctionReturnSpecConfigurer<RV> {

    private data class ReifiedPair<RV : ResourceView<*>, T : FunctionInputElement>(
        val elementClass: Class<T>,
        val resourceValueFunction: RValueFunction<RV, T>,
    )

    private var returnFunc: ReifiedPair<RV, *>? = null

    private val name = "$contextName.input"

    override fun <T : FunctionInputElement> returning(
        returnClass: Class<T>,
        configure: ResourceHelperFunctionInputSpecContext.(RV) -> T
    ): RValueFunction<RV, T> {
        return synchronized(this) {
            check(returnFunc == null) {
                "Function return spec '$name' already configured"
            }
            val context = BaseResourceHelperFunctionInputSpecContext()
            val func = RValueFunction.named<RV, T>(name, resourceViewClass) { resourceView ->
                context.configure(resourceView)
            }
            returnFunc = ReifiedPair(returnClass, func)

            /**
             * TODO: Obfuscate function wrapper from workspace
             */
            func
        }
    }
}