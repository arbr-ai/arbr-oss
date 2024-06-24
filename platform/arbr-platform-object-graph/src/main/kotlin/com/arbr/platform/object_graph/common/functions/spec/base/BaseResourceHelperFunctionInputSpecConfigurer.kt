package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

class BaseResourceHelperFunctionInputSpecConfigurer<RV : ResourceView<*>>(
    private val graphObjectQueryMapContextFactory: GraphObjectQueryMapContextFactory,
    private val resourceViewClass: Class<RV>,
    contextName: String,
) : ResourceHelperFunctionInputSpecConfigurer<RV> {
    private var inputProvider: RValueFunctionDelegateProvider<RV, *>? = null

    private val name = "$contextName.input"

    override fun <I : FunctionInputElement> inputting(
        inputClass: Class<I>,
        configure: GraphObjectQueryMapContext.(RV) -> I
    ): RValueFunctionDelegateProvider<RV, I> {
        return synchronized(this) {
            check(inputProvider == null) {
                "Function input spec '$name' already configured"
            }

            /**
             * TODO: Obfuscate function wrapper from workspace
             */
            val newProvider = RValueFunctionDelegateProvider { name ->
                val context = graphObjectQueryMapContextFactory.newContext()
                RValueFunction.named(name, resourceViewClass) { resourceView ->
                    context.configure(resourceView)
                }
            }

            inputProvider = newProvider
            newProvider
        }
    }
}