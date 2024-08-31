package com.arbr.platform.object_graph.common.model.view

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.types.naming.NamedResource
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.platform.object_graph.common.model.ProposedForeignKeyCollectionStream
import com.arbr.platform.object_graph.common.model.ProposedValueStream
import com.arbr.platform.object_graph.common.properties.FieldValueViewContainer
import com.arbr.platform.object_graph.common.properties.GenericWritableDelegate
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial

interface ProposedValueStreamViewProvider<RK : NamedResourceKey> {
    fun <ValueType, V : ObjectModel.ObjectValue<ValueType, *, *, V>> providePropertyView(
        objectType: ObjectModel.ObjectType<ValueType, *, *, V>,
        objectValueClass: Class<V>,
        proposedValueStream: ProposedValueStream<V>,
    ): GenericWritableDelegate<V>

    fun <
            R: NamedResource<*, RK, *, *>,
            RV: ResourceView<R>,
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > provideReferencePropertyView(
        resourceClass: Class<T>,
        resource: R,
        proposedValueStream: ProposedValueStream<ObjectRef<out T, P, ForeignKey>>,
    ): GenericWritableDelegate<RV>

    fun <R: NamedResource<*, RK, *, *>, RV: ResourceView<R>,
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > provideCollectionPropertyView(
        resourceClass: Class<T>,
        resource: R,
        proposedValueStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
    ): GenericWritableDelegate<FieldValueViewContainer<RV, ResourceView<*>, *>>
}
