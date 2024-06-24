package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext

class ResourceCommonFunctionConfigurerImpl<RV : ResourceView<*>>(
    private val resourceFunctionContext: ResourceFunctionContext,
    override val resourceViewClass: Class<RV>,
    private val contextName: String,
) : ResourceCommonFunctionConfigurer<RV> {
    private val graphObjectQueryMapContextFactory: GraphObjectQueryMapContextFactory =
        DefaultGraphObjectQueryMapContextFactory(resourceFunctionContext)
    private val inputConfigurerDelegate = BaseResourceHelperFunctionInputSpecConfigurer(
        graphObjectQueryMapContextFactory,
        resourceViewClass,
        contextName,
    )
    private val returnConfigurerDelegate = BaseResourceHelperFunctionReturnSpecConfigurer(
        resourceViewClass,
        contextName,
    )

    private var completionConfig: ResourceHelperFunctionCompleteSpecContext? = null
    private var searchConfig: DelegateProvider<GraphObjectEmbeddingSearchConfig>? = null
    private var searchQuerySpec: RValueFunctionDelegateProvider<RV, GraphObjectEmbeddingSearchSpec<out ResourceView<*>, out FunctionInputElement>>? =
        null

    override fun complete(configure: ResourceHelperFunctionCompleteSpecContext.() -> Unit) {
        synchronized(this) {
            check(completionConfig == null) {
                "Completion already configured for function $contextName"
            }
            // Context holds config for a completion
            // TODO: Crystallize to data model here
            completionConfig = ResourceHelperFunctionCompleteSpecContext().also(configure)
        }
    }

    override fun configuring(configure: ResourceEmbeddingHelperFunctionConfigContext.() -> Unit): DelegateProvider<GraphObjectEmbeddingSearchConfig> {
        val newSearchConfigProvider = synchronized(this) {
            check(searchConfig == null) {
                "Search already configured for function $contextName"
            }

            DelegateProviderImpl { property ->
                // Context holds config for a completion
                ResourceEmbeddingHelperFunctionConfigContextImpl()
                    .also(configure)
                    .toConfig()
            }
        }
        searchConfig = newSearchConfigProvider

        return newSearchConfigProvider
    }

    private fun getSearchConfig(): GraphObjectEmbeddingSearchConfig {
        return synchronized(this) {
            val provider = searchConfig ?: run {
                val config = ResourceEmbeddingHelperFunctionConfigContextImpl().toConfig()
                val provider = DelegateProviderImpl { _ -> config }
                provider.also { searchConfig = it }
            }
            provider.getOrConfigure()
        }
    }

    override fun <RVQ : ResourceView<*>, T : FunctionInputElement> querying(
        queryResourceViewClass: Class<RVQ>,
        configure: ResourceEmbeddingHelperFunctionQuerySpecContext.() -> HeterogeneousGraphObjectQuerySpec<RV, RVQ, T>
    ): RValueFunctionDelegateProvider<RV, GraphObjectEmbeddingSearchSpec<RVQ, T>> {
        return synchronized(this) {
            check(searchQuerySpec == null) {
                "Query already configured for function $contextName"
            }
            val context = ResourceEmbeddingHelperFunctionQuerySpecContext.new(
                graphObjectQueryMapContextFactory,
                contextName,
            )
            val querySpec = context.configure()

            RValueFunctionDelegateProvider { name ->
                RValueFunction.named(name, resourceViewClass) { resourceView ->
                    val embeddingSearchConfig = getSearchConfig()
                    val transformedInput = querySpec.inputAdapterQuerySpec.transform(resourceView)
                    GraphObjectEmbeddingSearchSpecImpl(
                        embeddingSearchConfig,
                        transformedInput,
                        querySpec.properQuerySpec,
                    )
                }
            }
        }
    }

    override fun <I : FunctionInputElement> inputting(
        inputClass: Class<I>,
        configure: GraphObjectQueryMapContext.(RV) -> I
    ): RValueFunctionDelegateProvider<RV, I> {
        return inputConfigurerDelegate.inputting(inputClass, configure)
    }

    override fun <T : FunctionInputElement> returning(
        returnClass: Class<T>,
        configure: ResourceHelperFunctionInputSpecContext.(RV) -> T
    ): RValueFunction<RV, T> {
        return returnConfigurerDelegate.returning(returnClass, configure)
    }
}