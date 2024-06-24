package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.types.ResourceStream
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider

/**
 * Traces dependencies for write operations
 */
interface WriteDependencyTracingProvider {

    fun <ValueType, T : ObjectModel.ObjectValue<ValueType, *, *, T>> setOuterValue(
        objectType: ObjectModel.ObjectType<ValueType, *, *, T>,
        containerIdentifier: PropertyIdentifier,
        value: T,
    )

    fun <
            RK : NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RV : ResourceView<R>,
            RS : ResourceStream<R>,
            > setOuterReferenceValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier,
        value: RV,
    )

    fun <
            RK : NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RV : ResourceView<R>,
            RS : ResourceStream<R>,
            > setOuterCollectionValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier,
        value: FieldValueViewContainer<RV, ResourceView<*>, *>,
    )

    fun collectWriteDependencies(): DependencyDescriptorSet
}