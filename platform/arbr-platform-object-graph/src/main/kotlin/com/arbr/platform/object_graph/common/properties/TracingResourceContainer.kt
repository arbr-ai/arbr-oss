package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier

/**
 * Queryable collection of OMRs, given special treatment because this is the format taken by child collection properties
 * Traced version - no actual backing map
 */
class TracingResourceContainer<E: RV, RV>(
    override val collectionRequirementsProvider: CollectionRequirementsProvider,
    override val collectionDependencyTracingValueProvider: CollectionDependencyTracingValueProvider<E>,
    override val containerIdentifier: CompoundPropertyIdentifier,
) : TracingContainer<E, RV> {

    override fun <E2 : RV> withProvider(
        provider: CollectionDependencyTracingValueProvider<E2>
    ): TracingResourceContainer<E2, RV> {
        return TracingResourceContainer(
            collectionRequirementsProvider,
            provider,
            containerIdentifier
        )
    }

    override fun <E2 : RV> plusContainer(other: FieldValueViewContainer<E2, RV, TracingContainer<out RV, RV>>): TracingContainer<out RV, RV> {
        val nextContainerId = CompoundPropertyIdentifier.UnionOf(containerIdentifier, other.containerIdentifier)
        return TracingResourceContainer(
            collectionRequirementsProvider,
            collectionDependencyTracingValueProvider,
            nextContainerId,
        )
    }
}
