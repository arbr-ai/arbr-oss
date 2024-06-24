package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.types.ResourceStream
import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.TypedResourceViewProvider
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import java.util.*

class ValuelessDependencyTracingValueProvider<ValueType>(
    private val containerIdentifier: PropertyIdentifier,
): DependencyTracingValueProvider<ValueType> {
    override fun provideValue(): ValueType {
        throw DependencyTracingValueException(
            containerIdentifier,
        )
    }

    override fun <W> transformWith(f: (ValueType) -> W): DependencyTracingValueProvider<W> {
        // TODO: Reflect transform in container ID
        return ValuelessDependencyTracingValueProvider(containerIdentifier)
    }
}

class DefaultReadDependencyTracingProvider(
    private val resourceViewProviderFactory: ResourceViewProviderFactory,
    private val resourceStreamProviderFactory: ResourceStreamProviderFactory,
) : ReadDependencyTracingProvider {
    private val dependencies = mutableListOf<DependencyDescriptor>()
    private val collectionRequirementsProvider = DefaultTracingCollectionRequirementsProvider()

    // Hack to avoid a big refactor: Keep track of the ObjectValue UUID -> Property ID mapping for this tracer to
    // recover property IDs when needed
    private val objectValuePropertyIdentifierMap = mutableMapOf<String, PropertyIdentifier>()

    private fun newUuid() = UUID.randomUUID().toString()

    private fun addDependency(dependencyDescriptor: DependencyDescriptor) {
        synchronized(this) {
            dependencies.add(dependencyDescriptor)
        }
    }

    private fun <
            RK : NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RV : ResourceView<R>
            > getResourceViewProvider(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R
    ): TypedResourceViewProvider<R, RV> {
        return resourceViewProviderFactory.resourceViewProvider(proposedValueStreamViewProvider, resource)
    }

    override fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> getInnerValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier,
    ): ValueType {
        throw DependencyTracingValueException(
            containerIdentifier,
        )
    }

    private fun <ValueType, T : ObjectModel.ObjectValue<ValueType?, *, *, T>> getInnerNonNullValue(
        objectValue: ObjectModel.ObjectValue<ValueType?, *, *, T>,
    ): ValueType {
        throw DependencyTracingNullValueException(
            objectValue,
        )
    }

    /**
     * Map an outer value with a nullable inner value to one with a non-null inner value, acknowledging the relevant
     * dependency.
     */
    override fun <ValueType : Any, T : ObjectModel.ObjectValue<ValueType?, *, *, T>> getNonNullValue(
        objectValue: ObjectModel.ObjectValue<ValueType?, *, *, T>
    ): SourcedValue<ValueType> {
        val propertyIdentifier = synchronized(this) {
            objectValuePropertyIdentifierMap[objectValue.id]
        } ?: throw IllegalStateException("Attempt to de-null unrecognized object value ${objectValue.id}")

        addDependency(NonNullDependencyDescriptor(propertyIdentifier))

        val tracingValueProvider = ValuelessDependencyTracingValueProvider<ValueType>(propertyIdentifier)
        return SourcedTracingValue(
            newUuid(),
            objectValue.kind,
            tracingValueProvider,
            objectValue.typeName,
            objectValue.schema.requiredNonNull(),
            objectValue.generatorInfo,
        )
    }

    override fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> getOuterValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier,
    ): T {
        addDependency(PropertyValueExistenceDependencyDescriptor(containerIdentifier))
        val tracingValueProvider: DependencyTracingValueProvider<ValueType> = ValuelessDependencyTracingValueProvider(containerIdentifier)
        return objectType.trace(
            SourcedValueKind.COMPUTED,
            SourcedValueGeneratorInfo(emptyList()),
            tracingValueProvider
        ).also { objectValue ->
            synchronized(this) {
                objectValuePropertyIdentifierMap[objectValue.id] = containerIdentifier
            }
        }
    }

    private fun <
            RK : NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RS : ResourceStream<R>,
            RV : ResourceView<R>
            > getResourceView(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R
    ): RV {
        val provider = getResourceViewProvider<RK, R, RV>(proposedValueStreamViewProvider, resource)
        val uuid = newUuid()
        val emptyResource = resourceStreamProviderFactory
            .resourceStreamProvider<R, RS>(resource)
            .provideEmptyResource(
                uuid
            )
        return provider.provideResourceView(emptyResource)
    }

    override fun <RK : NamedResourceKey, R : NamedResource<*, RK, *, *>, RV : ResourceView<R>, RS : ResourceStream<R>> getOuterReferenceValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier
    ): RV {
        addDependency(PropertyReferenceValueExistenceDependencyDescriptor(containerIdentifier))
        return getResourceView(proposedValueStreamViewProvider, resource)
    }

    override fun <
            RK : NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RV : ResourceView<R>
            > getOuterCollectionValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier
    ): TracingResourceContainer<RV, ResourceView<*>> {
        addDependency(PropertyCollectionValueExistenceDependencyDescriptor(containerIdentifier))
        val collectionDependencyTracingValueProvider = CollectionDependencyTracingValueProvider<RV> { propertyIdentifier ->
            addDependency(PropertyReferenceValueExistenceDependencyDescriptor(propertyIdentifier))
            getResourceView(proposedValueStreamViewProvider, resource)
        }

        return TracingResourceContainer(
            collectionRequirementsProvider,
            collectionDependencyTracingValueProvider,
            containerIdentifier,
        )
    }

    override fun collectReadDependencies(): DependencyDescriptorSet {
        val collectionRequirements = collectionRequirementsProvider
            .getCollectionRequirements()
        val readDependencies = synchronized(this) {
            dependencies.toList()
        }

        return DependencyDescriptorSet(readDependencies + collectionRequirements)
    }

}

