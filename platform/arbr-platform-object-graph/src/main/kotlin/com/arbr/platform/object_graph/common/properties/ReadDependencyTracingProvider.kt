package com.arbr.platform.object_graph.common.properties

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedResource
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.platform.object_graph.common.model.PropertyIdentifier
import com.arbr.platform.object_graph.common.model.view.ProposedValueStreamViewProvider

interface ReadDependencyTracingProvider: ReferenceDependencyTracingProvider, NonNullRequirementsProvider {

    fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> getInnerValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier,
    ): ValueType

    fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> getOuterValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier,
    ): T

    fun <
            RK : NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RV : ResourceView<R>,
            > getOuterCollectionValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier,
    ): TracingResourceContainer<RV, ResourceView<*>>

    fun collectReadDependencies(): DependencyDescriptorSet
}
