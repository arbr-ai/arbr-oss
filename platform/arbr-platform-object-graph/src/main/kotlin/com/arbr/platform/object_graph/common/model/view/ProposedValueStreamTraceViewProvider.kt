package com.arbr.og.object_model.common.model.view

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.og.object_model.common.model.ProposedValueStream
import com.arbr.og.object_model.common.properties.*
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial

class ProposedValueStreamTraceViewProvider<RK: NamedResourceKey>(
    private val readDependencyTracingProvider: ReadDependencyTracingProvider,
    private val writeDependencyTracingProvider: WriteDependencyTracingProvider,
) : ProposedValueStreamViewProvider<RK> {

    override fun <ValueType, V : ObjectModel.ObjectValue<ValueType, *, *, V>> providePropertyView(
        objectType: ObjectModel.ObjectType<ValueType, *, *, V>,
        objectValueClass: Class<V>,
        proposedValueStream: ProposedValueStream<V>,
    ): GenericWritableDelegate<V> {
        return delegate {
            readDependencyTracingProvider.getOuterValue(
                objectType,
                proposedValueStream.identifier,
            )
        }.writeWith { newValue ->
            writeDependencyTracingProvider.setOuterValue(
                objectType,
                proposedValueStream.identifier,
                newValue,
            )
        }
    }

    override fun <
            R: NamedResource<*, RK, *, *>,
            RV: ResourceView<R>,
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > provideReferencePropertyView(
        resourceClass: Class<T>,
        resource: R,
        proposedValueStream: ProposedValueStream<ObjectRef<out T, P, ForeignKey>>,
    ): GenericWritableDelegate<RV> {
        return delegate<RV> {
            readDependencyTracingProvider.getOuterReferenceValue(
                this,
                resource,
                proposedValueStream.identifier,
            )
        }.writeWith { newResourceView ->
            writeDependencyTracingProvider.setOuterReferenceValue(
                this,
                resource,
                proposedValueStream.identifier,
                newResourceView,
            )
        }
    }

    override fun <
            R: NamedResource<*, RK, *, *>,
            RV: ResourceView<R>,
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > provideCollectionPropertyView(
        resourceClass: Class<T>,
        resource: R,
        proposedValueStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
    ): GenericWritableDelegate<FieldValueViewContainer<RV, ResourceView<*>, *>> {
        return delegate<FieldValueViewContainer<RV, ResourceView<*>, *>> {
            readDependencyTracingProvider.getOuterCollectionValue(
                this,
                resource,
                proposedValueStream.identifier,
            )
        }.writeWith { viewCollection ->
            writeDependencyTracingProvider.setOuterCollectionValue(
                this,
                resource,
                proposedValueStream.identifier,
                viewCollection,
            )
        }
    }

}