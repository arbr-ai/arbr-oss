package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier

class ConcreteCollectionRequirementsProvider(
    private val backingCollectionSize: Int,
): CollectionRequirementsProvider {
    override fun requireSizeAtLeast(containerIdentifier: CompoundPropertyIdentifier, minSize: Int) {
        if (backingCollectionSize < minSize) {
            throw CollectionRequirementsException()
        }
    }

    override fun requireSizeAtMost(containerIdentifier: CompoundPropertyIdentifier, maxSize: Int) {
        if (backingCollectionSize > maxSize) {
            throw CollectionRequirementsException()
        }
    }

    override fun getCollectionRequirements(): List<CollectionRequirements> {
        // Not a tracing context, so we don't track requirements
        return emptyList()
    }
}