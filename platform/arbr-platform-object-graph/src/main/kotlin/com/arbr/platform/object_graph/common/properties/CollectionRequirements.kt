package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier

data class CollectionRequirements(
    override val propertyIdentifier: CompoundPropertyIdentifier,
    val lowerSizeBound: Int?,
    val upperSizeBound: Int?,
): DependencyDescriptor(propertyIdentifier)