package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedPropertyKey
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext
import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier
import com.arbr.og.object_model.common.model.PropertyIdentifierBase
import com.arbr.og.object_model.common.model.PropertyKeyRelationship
import com.arbr.og.object_model.common.properties.ConcreteFieldValueViewContainer
import com.arbr.og.object_model.common.properties.FieldValueViewContainer
import java.util.*

abstract class BaseRFunctionSpecifier<RV : ResourceView<*>>(
    private val resourceViewClass: Class<RV>,
) : RFunctionSpecifier<RV> {
    private val resourceCompletionHelperFunctionConfigurerFactory: ResourceCommonFunctionConfigurerFactory =
        DefaultResourceCommonFunctionConfigurerFactory()

    private val providers = mutableListOf<RValueFunctionConfigurableDelegateProvider<RV, *>>()

    private val configurableMutators = mutableListOf<RFunctionConfigurable<RV>>()
    private val configurableHelpers = mutableListOf<RValueFunctionConfigurable<RV, *>>()

    private fun addMutator(configurableFunction: RFunctionConfigurable<RV>) {
        synchronized(this) {
            configurableMutators.add(configurableFunction)
        }
    }

    private fun addHelper(configurableFunction: RValueFunctionConfigurable<RV, *>) {
        synchronized(this) {
            configurableHelpers.add(configurableFunction)
        }
    }

    private fun makeConfigurable(
        name: String,
        mutatorFunction: (ResourceFunctionContext.(RV) -> Unit)
    ): RFunctionConfigurable<RV> {
        return RFunctionConfigurable<RV> { configurators ->
            val func = RValueFunction.named(name, resourceViewClass) { resourceView ->
                mutatorFunction(configurators, resourceView)
            }

            func
        }
    }

    private fun <T : FunctionInputElement> makeConfigurableWithValue(
        name: String,
        configurerFunction: (ResourceCommonFunctionConfigurer<RV>.() -> RValueFunction<RV, T>)
    ): RValueFunctionConfigurable<RV, T> {
        return RValueFunctionConfigurable<RV, T> { context ->
            val configurer =
                resourceCompletionHelperFunctionConfigurerFactory.makeResourceCommonFunctionConfigurer(
                    context,
                    resourceViewClass,
                    name,
                )
            configurerFunction(configurer)
        }
    }

    private fun <T : FunctionInputElement> makeCommonHelperFunctionProvider(
        f: ResourceCommonFunctionConfigurer<RV>.() -> RValueFunction<RV, T>
    ): RValueFunctionConfigurableDelegateProvider<RV, T> {
        return RValueFunctionConfigurableDelegateProvider(resourceViewClass) { name ->
            makeConfigurableWithValue(name, f).also(this::addHelper)
        }.also(providers::add)
    }

    override fun <T : FunctionInputElement> helper(f: ResourceHelperFunctionConfigurer<RV>.() -> RValueFunction<RV, T>): RValueFunctionConfigurableDelegateProvider<RV, T> {
        return makeCommonHelperFunctionProvider(f)
    }

    override fun <T : FunctionInputElement, RVQ : ResourceView<*>> embedding(
        f: ResourceEmbeddingHelperFunctionConfigurer<RV>.() -> RValueFunction<RV, GraphObjectEmbeddingSearchSpec<RVQ, T>>
    ): RValueFunctionConfigurableDelegateProvider<RV, FieldValueViewContainer<RVQ, RVQ, *>> {
        return RValueFunctionConfigurableDelegateProvider(resourceViewClass) { name ->
            RValueFunctionConfigurable<RV, FieldValueViewContainer<RVQ, RVQ, *>> { context ->
                val configurer =
                    resourceCompletionHelperFunctionConfigurerFactory.makeResourceCommonFunctionConfigurer(
                        context,
                        resourceViewClass,
                        name,
                    )
                val searchSpecFunction = f(configurer)

                RValueFunction.async(searchSpecFunction.name, searchSpecFunction.resourceViewClass) { resourceView ->
                    val searchSpecMono = searchSpecFunction.apply(resourceView)

                    searchSpecMono.map { searchSpec ->
                        // Always trace?
                        val queriedResourceView = context.resourceViewInstantiators
                            .instantiator<RVQ>(searchSpec.querySpec.pivotClass)
                            .newResource()
                        ConcreteFieldValueViewContainer(
                            listOf(queriedResourceView),
                            // TODO: design a better identifier
                            containerPlaceholderPropertyIdentifier(),
                        )
                    }
                }
            }.also(this::addHelper)
        }.also(providers::add)
    }

    private fun containerPlaceholderPropertyIdentifier(): CompoundPropertyIdentifier {
        return PropertyIdentifierBase(
            object : NamedResourceKey {
                override val name: String = ""
                override val ordinal: Int = 0
            },
            object : NamedPropertyKey {
                override val name: String = ""
                override val ordinal: Int = 0
            },
            PropertyKeyRelationship.CHILD_COLLECTION,
            UUID.randomUUID().toString(),
        )
    }

    override fun <T : FunctionInputElement> completion(f: ResourceCompletionHelperFunctionConfigurer<RV>.() -> RValueFunction<RV, T>): RValueFunctionConfigurableDelegateProvider<RV, T> {
        return makeCommonHelperFunctionProvider(f)
    }

    override fun mutating(mutate: (ResourceFunctionContext.(RV) -> Unit)): RValueFunctionConfigurableDelegateProvider<RV, Unit> {
        val provider = RValueFunctionConfigurableDelegateProvider(resourceViewClass) { name ->
            val configurable = makeConfigurable(name, mutate)
            addMutator(configurable)
            configurable
        }
        providers.add(provider)
        return provider
    }

    fun <RK: NamedResourceKey> buildConfigurableFunctionSet(
        functionSetName: String,
    ): RFunctionConfigurableSet<RV, RK> {
        val configurableMutators = configurableMutators.toList()

        // TODO: Wire together helpers and mutators
        val helpers = configurableHelpers.map { func ->
            RFunctionConfigurable { context ->
                val valuedFunc = func.configure(context)
                RValueFunction.async(valuedFunc.name, valuedFunc.resourceViewClass) {
                    valuedFunc.apply(it).thenReturn(Unit)
                }
            }
        }

        return RFunctionConfigurableSet(functionSetName, configurableMutators + helpers)
    }

    fun collectConfigurableFunctions() = providers.map {
        it.getOrConfigure()
    }
}