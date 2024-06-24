package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.types.ResourceStream
import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider
import com.arbr.og.object_model.common.values.SourcedValue
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
class DefaultMapReadDependencyTracingProvider(
    resourceViewProviderFactory: ResourceViewProviderFactory,
    resourceStreamProviderFactory: ResourceStreamProviderFactory,
    private val valueMap: ConcurrentHashMap<PropertyIdentifier, Any>,
) : ReadDependencyTracingProvider {
    private val delegate =
        DefaultReadDependencyTracingProvider(resourceViewProviderFactory, resourceStreamProviderFactory)

    override fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> getInnerValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier
    ): ValueType {
        return valueMap.compute(containerIdentifier) { key, existing ->
            val existingAs = existing as? ValueType
            existingAs ?: delegate.getInnerValue(objectType, key)
        } as ValueType
    }

    override fun <ValueType : Any, T : ObjectModel.ObjectValue<ValueType?, *, *, T>> getNonNullValue(
        objectValue: ObjectModel.ObjectValue<ValueType?, *, *, T>
    ): SourcedValue<ValueType> {
        return delegate.getNonNullValue(objectValue)
    }

    override fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> getOuterValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier
    ): T {
        return valueMap.compute(containerIdentifier) { key, existing ->
            val existingAs = existing as? T
            existingAs ?: delegate.getOuterValue(objectType, key)
        } as T
    }

    override fun <RK : NamedResourceKey, R : NamedResource<*, RK, *, *>, RV : ResourceView<R>> getOuterCollectionValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier
    ): TracingResourceContainer<RV, ResourceView<*>> {
        return valueMap.compute(containerIdentifier) { key, existing ->
            val existingAs = existing as? TracingResourceContainer<RV, RV>
            existingAs ?: delegate.getOuterCollectionValue(
                proposedValueStreamViewProvider,
                resource,
                key
            )
        } as TracingResourceContainer<RV, ResourceView<*>>
    }

    override fun <RK : NamedResourceKey, R : NamedResource<*, RK, *, *>, RV : ResourceView<R>, RS : ResourceStream<R>> getOuterReferenceValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier
    ): RV {
        return valueMap.compute(containerIdentifier) { key, existing ->
            val existingAs = existing as? RV
            existingAs ?: delegate.getOuterReferenceValue<RK, R, RV, RS>(proposedValueStreamViewProvider, resource, key)
        } as RV
    }

    override fun collectReadDependencies(): DependencyDescriptorSet {
        return delegate.collectReadDependencies()
    }
}