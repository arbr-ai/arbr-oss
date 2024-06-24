package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier

interface CollectionRequirementsProvider {

    fun requireSizeAtLeast(containerIdentifier: CompoundPropertyIdentifier, minSize: Int)

    fun requireSizeAtMost(containerIdentifier: CompoundPropertyIdentifier, maxSize: Int)

    fun requireNonempty(containerIdentifier: CompoundPropertyIdentifier) {
        return requireSizeAtLeast(containerIdentifier, 1)
    }

    fun requireEmpty(containerIdentifier: CompoundPropertyIdentifier) {
        return requireSizeAtMost(containerIdentifier, 0)
    }

    fun getCollectionRequirements(): List<CollectionRequirements>
}
