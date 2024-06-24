package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.resource.ArbrCommitEval
import com.arbr.object_model.core.types.Arbr
import com.arbr.object_model.core.types.ArbrResourceKey
import com.arbr.object_model.core.types.ArbrResourceKeyResolver
import com.arbr.object_model.core.types.ResourceStreamProviders
import com.arbr.object_model.core.types.ResourceViewProviders
import com.arbr.object_model.core.types.TypedResourceViewProvider
import com.arbr.object_model.core.types.suites.ResourceAssociatedObjectCollection
import com.arbr.object_model.core.types.suites.ResourceAssociatedObjectCollectionBuilder
import com.arbr.object_model.core.view.ArbrCommitEvalView
import com.arbr.object_model.core.view.ArbrCommitView
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProviderFactory
import com.arbr.og.object_model.common.requirements.DefaultRequirementsProvider
import com.arbr.platform.ml.linear.typed.shape.Shape
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FieldValueViewContainerTest {
    private val resourceViewProviderFactory = ResourceViewProviders()
    private val resourceStreamProviderFactory = ResourceStreamProviders()
    private val defaultDependencyTracingProvider = DefaultMapReadWriteDependencyTracingProvider(
        resourceViewProviderFactory,
        resourceStreamProviderFactory
    )
    private val resourceKeyResolver = ArbrResourceKeyResolver
    private val resourceAssociatedObjectCollectionBuilder = object : ResourceAssociatedObjectCollectionBuilder<ArbrResourceKey, TypedResourceViewProvider<*, *>> {
        override fun buildWith(transform: (ArbrResourceKey) -> TypedResourceViewProvider<*, *>): ResourceAssociatedObjectCollection<ArbrResourceKey, TypedResourceViewProvider<*, *>> {
            @Suppress("EnumValuesSoftDeprecate") val enumValues = ArbrResourceKey.values()
            return ResourceAssociatedObjectCollection.new(enumValues, transform)
        }
    }

    private val proposedValueStreamViewProviderFactory = ProposedValueStreamViewProviderFactory(
        requirementsProvider = DefaultRequirementsProvider(),
        resourceViewProviderFactory = resourceViewProviderFactory,
        readDependencyTracingProvider = defaultDependencyTracingProvider,
        writeDependencyTracingProvider = defaultDependencyTracingProvider,
        resourceKeyResolver = resourceKeyResolver,
        resourceAssociatedObjectCollectionBuilder = resourceAssociatedObjectCollectionBuilder,
    )
    private val proposedValueStreamProvider = proposedValueStreamViewProviderFactory.provider(ProposedValueAccessTier.TRACE)

    fun <V, ST: Shape, SF: Shape, U : ObjectModel.ObjectValue<V, ST, SF, U>> ObjectModel.ObjectValue<V, ST, SF, *>.to(
        targetType: ObjectModel.ObjectType<V, ST, SF, U>,
    ): U {
        return this.map(targetType) { it }
    }

    @Test
    fun maps() {
        val viewProvider: TypedResourceViewProvider<Arbr.Commit, ArbrCommitView> = resourceViewProviderFactory.resourceViewProvider(
            proposedValueStreamProvider,
            Arbr.Commit,
        )
        val resourceStream =
            resourceStreamProviderFactory.resourceStreamProvider(Arbr.Commit).provideEmptyResource("uuid")
        val resourceView = viewProvider.provideResourceView(resourceStream)

        val oldValue = resourceView.commitEvals
        val setValue = resourceView.commitEvals.map { arbrCommitEvalView ->
            arbrCommitEvalView
        }
        resourceView.commitEvals = setValue

        val newValue = resourceView.commitEvals
        Assertions.assertEquals(setValue, newValue)
        Assertions.assertNotEquals(oldValue, newValue)
    }

    @Test
    fun `maps from another type`() {
        val viewProvider: TypedResourceViewProvider<Arbr.Commit, ArbrCommitView> = resourceViewProviderFactory.resourceViewProvider(
            proposedValueStreamProvider,
            Arbr.Commit,
        )
        val resourceStream =
            resourceStreamProviderFactory.resourceStreamProvider(Arbr.Commit).provideEmptyResource("uuid")
        val resourceView = viewProvider.provideResourceView(resourceStream)

        val oldValue = resourceView.commitEvals
        val setValue = resourceView.fileOps.map { arbrFileOpView ->
            val evalViewProvider: TypedResourceViewProvider<Arbr.CommitEval, ArbrCommitEvalView> = resourceViewProviderFactory.resourceViewProvider(
                proposedValueStreamProvider,
                Arbr.CommitEval,
            )
            val evalStream =
                resourceStreamProviderFactory.resourceStreamProvider(Arbr.CommitEval).provideEmptyResource("uuid-eval")
            val evalView = evalViewProvider.provideResourceView(evalStream)

            val fileOpValue = arbrFileOpView.fileOperation.map {
                "Hello"
            }
            evalView.errorContent = fileOpValue.to(ArbrCommitEval.ErrorContent)

            evalView
        }
        val sum = setValue + setValue

        sum.requireSizeAtLeast(4)
        resourceView.commitEvals = sum

        val newValue = resourceView.commitEvals
        Assertions.assertEquals(sum, newValue)
        Assertions.assertNotEquals(oldValue, newValue)

        val collectDependencies = defaultDependencyTracingProvider.collectDependencies()
        println("Read dependencies:")
        collectDependencies.readDependencySet.dependencies.forEach { dependencyDescriptor ->
            println(dependencyDescriptor)
        }
        println("Write dependencies:")
        collectDependencies.writeDependencySet.dependencies.forEach { dependencyDescriptor ->
            println(dependencyDescriptor)
        }
    }

}

