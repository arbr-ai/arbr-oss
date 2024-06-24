package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier
import com.arbr.og.object_model.common.values.SourcedValue

class TracingPropertyContainer<ValueType, V : SourcedValue<ValueType>>(
    override val collectionRequirementsProvider: CollectionRequirementsProvider,
    override val collectionDependencyTracingValueProvider: CollectionDependencyTracingValueProvider<V>,
    override val containerIdentifier: CompoundPropertyIdentifier,
) : TracingContainer<V, SourcedValue<ValueType>> {

    override fun <E2 : SourcedValue<ValueType>> withProvider(
        provider: CollectionDependencyTracingValueProvider<E2>
    ): TracingContainer<E2, SourcedValue<ValueType>> {
        val tracingPropertyContainer = TracingPropertyContainer(
            collectionRequirementsProvider,
            provider,
            containerIdentifier
        )
        return tracingPropertyContainer
    }

    override fun <E2 : SourcedValue<ValueType>> plusContainer(
        other: FieldValueViewContainer<E2, SourcedValue<ValueType>, TracingContainer<out SourcedValue<ValueType>, SourcedValue<ValueType>>>
    ): TracingContainer<out SourcedValue<ValueType>, SourcedValue<ValueType>> {
        val nextContainerId = CompoundPropertyIdentifier.UnionOf(containerIdentifier, other.containerIdentifier)
        return TracingPropertyContainer(
            collectionRequirementsProvider,
            collectionDependencyTracingValueProvider,
            nextContainerId,
        )
    }
}
