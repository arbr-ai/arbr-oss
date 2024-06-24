package com.arbr.og.object_model.common.model.view

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.types.ResourceStream
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.TypedResourceViewProvider
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.object_model.core.types.suites.ResourceAssociatedObjectCollectionBuilder
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.og.object_model.common.model.ProposedValueStream
import com.arbr.og.object_model.common.properties.ConcreteFieldValueViewContainer
import com.arbr.og.object_model.common.properties.FieldValueViewContainer
import com.arbr.og.object_model.common.properties.GenericWritableDelegate
import com.arbr.og.object_model.common.properties.delegate
import com.arbr.og.object_model.common.requirements.AccessContextRequirementsProvider
import com.arbr.og.object_model.common.requirements.AccessContextWriteRequirementsProvider
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.platform.object_graph.util.cls

sealed class ProposedValueStreamAccessContextViewProvider<RK : NamedResourceKey>(
    private val requirementsProvider: AccessContextRequirementsProvider,
    private val writeRequirementsProvider: AccessContextWriteRequirementsProvider,
    private val resourceViewProviderFactory: ResourceViewProviderFactory,
    private val resourceKeyResolver: ResourceKeyResolver<RK>,
    resourceAssociatedObjectCollectionBuilder: ResourceAssociatedObjectCollectionBuilder<RK, TypedResourceViewProvider<*, *>>,
) : ProposedValueStreamViewProvider<RK> {
    private val viewProviders = resourceAssociatedObjectCollectionBuilder
        .buildWith { resourceKey ->
            val resource = resourceKeyResolver.resolveKey(resourceKey)

            resourceViewProviderFactory
                .resourceViewProvider(
                    this,
                    resource,
                )
        }

    @Suppress("UNCHECKED_CAST")
    private fun <R : NamedResource<*, RK, *, *>, RV : ResourceView<R>> getResourceViewProvider(resource: R): TypedResourceViewProvider<R, RV> {
        return viewProviders.getAssociatedObject(resource.resourceKey) as TypedResourceViewProvider<R, RV>
    }

    override fun <ValueType, V : ObjectModel.ObjectValue<ValueType, *, *, V>> providePropertyView(
        objectType: ObjectModel.ObjectType<ValueType, *, *, V>,
        objectValueClass: Class<V>,
        proposedValueStream: ProposedValueStream<V>
    ): GenericWritableDelegate<V> {
        return delegate<V> {
            requirementsProvider.require<V>(
                proposedValueStream,
                objectValueClass,
            )
        }.writeWith { newValue ->
            writeRequirementsProvider.requireWriteTo<V>(
                proposedValueStream,
                objectValueClass,
                newValue,
            )
        }
    }

    override fun <R: NamedResource<*, RK, *, *>, RV: ResourceView<R>,
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > provideCollectionPropertyView(
        resourceClass: Class<T>,
        resource: R,
        proposedValueStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
    ): GenericWritableDelegate<FieldValueViewContainer<RV, ResourceView<*>, *>> {
        val provider: TypedResourceViewProvider<R, RV> = getResourceViewProvider(resource)

        return delegate {
            val innerCollection = requirementsProvider.requireChildren<T, P, ForeignKey>(
                proposedValueStream,
                resourceClass,
            ).mapValues { (_, objectModelResource) ->
                @Suppress("UNCHECKED_CAST")
                provider.provideResourceView(
                    objectModelResource as ResourceStream<R>
                )
            }
                .values

            val container: FieldValueViewContainer<RV, ResourceView<*>, *> = ConcreteFieldValueViewContainer(
                innerCollection,
                proposedValueStream.identifier
            )

            container
        }.writeWith { newValue ->
            if (newValue is ConcreteFieldValueViewContainer<RV, ResourceView<*>>) {
                // Encode resource views back into an object ref map
                val refMapValue: ImmutableLinkedMap<String, ObjectRef<out T, P, ForeignKey>> = ImmutableLinkedMap(
                    newValue.backingCollection.map {
                        it.uuid to ObjectRef.OfUuid<T, P, ForeignKey>(it.uuid)
                    }
                )

                writeRequirementsProvider.requireWriteToChildren<T, P, ForeignKey>(
                    proposedValueStream,
                    resourceClass,
                    refMapValue,
                )
            } else {
                throw IllegalStateException("Unexpected field view container type")
            }
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
        val provider: TypedResourceViewProvider<R, RV> = getResourceViewProvider(resource)

        return delegate<RV> {
            val tClass = cls<ObjectRef<out T, P, ForeignKey>>()
            val objectModelResource = requirementsProvider.requireAttached<T, P, ForeignKey>(
                proposedValueStream,
                tClass,
            )

            @Suppress("UNCHECKED_CAST")
            provider.provideResourceView(
                objectModelResource as ResourceStream<R>
            )
        }.writeWith {
            val refClass = cls<ObjectRef<out T, P, ForeignKey>>()
            writeRequirementsProvider.requireWriteToAttached<T, P, ForeignKey>(
                proposedValueStream,
                refClass,
                ObjectRef.OfUuid<T, P, ForeignKey>(it.uuid),
            )
        }
    }

}