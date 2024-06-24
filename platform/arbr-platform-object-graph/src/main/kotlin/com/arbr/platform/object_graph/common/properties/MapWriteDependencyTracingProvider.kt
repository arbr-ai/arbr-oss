package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.types.ResourceStream
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider
import java.util.concurrent.ConcurrentHashMap

class MapWriteDependencyTracingProvider(
    private val valueMap: ConcurrentHashMap<PropertyIdentifier, Any>,
) : WriteDependencyTracingProvider {
    private val dependencies = mutableListOf<DependencyDescriptor>()

    private fun addDependency(dependencyDescriptor: DependencyDescriptor) {
        synchronized(this) {
            dependencies.add(dependencyDescriptor)
        }
    }

    override fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> setOuterValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier,
        value: T
    ) {
        addDependency(PropertyValueExistenceDependencyDescriptor(containerIdentifier))
        valueMap[containerIdentifier] = value
    }

    override fun <RK : NamedResourceKey, R : NamedResource<*, RK, *, *>, RV : ResourceView<R>, RS : ResourceStream<R>> setOuterReferenceValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier,
        value: RV
    ) {
        addDependency(PropertyReferenceValueExistenceDependencyDescriptor(containerIdentifier))
        valueMap[containerIdentifier] = value
    }

    override fun <RK : NamedResourceKey, R : NamedResource<*, RK, *, *>, RV : ResourceView<R>, RS : ResourceStream<R>> setOuterCollectionValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier,
        value: FieldValueViewContainer<RV, ResourceView<*>, *>
    ) {
        addDependency(PropertyCollectionValueExistenceDependencyDescriptor(containerIdentifier))
        valueMap[containerIdentifier] = value
    }

    override fun collectWriteDependencies(): DependencyDescriptorSet {
        val writeDependencies = synchronized(this) {
            dependencies.toList()
        }
        return DependencyDescriptorSet(writeDependencies)
    }
}
