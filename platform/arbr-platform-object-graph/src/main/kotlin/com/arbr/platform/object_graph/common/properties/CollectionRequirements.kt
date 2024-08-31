package com.arbr.platform.object_graph.common.properties

import com.arbr.platform.object_graph.common.model.CompoundPropertyIdentifier

data class CollectionRequirements(
    override val propertyIdentifier: CompoundPropertyIdentifier,
    val lowerSizeBound: Int?,
    val upperSizeBound: Int?,
): DependencyDescriptor(propertyIdentifier)