package com.arbr.platform.object_graph.common.model

import com.arbr.platform.object_graph.types.naming.NamedPropertyKey
import com.arbr.platform.object_graph.types.naming.NamedResourceKey

data class PropertyDetachedIdentifierBase(
    override val resourceKey: NamedResourceKey,
    override val propertyKey: NamedPropertyKey,
    override val relationship: PropertyKeyRelationship,
): PropertyDetachedIdentifier