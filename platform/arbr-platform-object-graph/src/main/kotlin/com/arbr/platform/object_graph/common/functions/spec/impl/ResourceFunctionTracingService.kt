package com.arbr.og.object_model.common.functions.spec.impl

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ResourceFunctionTracingService {

    private val resourceFunctionConfig by lazy {
        ResourceFunctionConfigurationLoader().load()
    }

    private val dependencyTracingProviderFactory = DependencyTracingProviderFactoryImpl(
        resourceFunctionConfig.resourceViewProviderFactory,
        resourceFunctionConfig.resourceStreamProviderFactory,
    )

    private val tracer = RFunctionDependencyTracerImpl(
        resourceFunctionConfig.resourceViewProviderFactory,
        resourceFunctionConfig.resourceStreamProviderFactory,
        dependencyTracingProviderFactory,
        resourceFunctionConfig.resourceViewInstantiatorsFactory,
    )

    fun traceConfiguredResourceFunctions(): Mono<Void> {
        val defaultRFunctionFactory = DefaultRFunctionFactory()
        resourceFunctionConfig.functions.forEach { resourceFunction ->
            resourceFunction.configure(defaultRFunctionFactory)
        }

        val rFunctionConfigurableSets = defaultRFunctionFactory
            .collectConfigurableFunctionSets()

        return Flux.fromIterable(rFunctionConfigurableSets).concatMap { rFunctionConfigurableSet ->
            val configuredDependenciesMono = tracer.trace(rFunctionConfigurableSet)

            configuredDependenciesMono.map { configuredDependencies ->
                configuredDependencies.forEach { rFunctionConfiguredDependencies ->
                    println(rFunctionConfiguredDependencies.resourceFunction.name)
                    println(rFunctionConfiguredDependencies.resourceFunction.resourceViewClass)

                    println("Read dependencies:")
                    rFunctionConfiguredDependencies.dependencyDescriptorSuite.readDependencySet.dependencies.forEach { dep ->
                        println(dep)
                    }

                    println("Write dependencies:")
                    rFunctionConfiguredDependencies.dependencyDescriptorSuite.writeDependencySet.dependencies.forEach { dep ->
                        println(dep)
                    }
                }
            }
        }
            .then()
    }
}