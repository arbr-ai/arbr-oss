package com.arbr.platform.object_graph.common.model

import com.arbr.platform.object_graph.common.model.PropertyKeyRelationship
import com.arbr.platform.object_graph.types.naming.NamedPropertyKey
import com.arbr.platform.object_graph.types.naming.NamedResourceKey

/**
 * Property "slot" identifier, independent of the particular resource
 */
interface PropertyDetachedIdentifier {
    val resourceKey: NamedResourceKey
    val propertyKey: NamedPropertyKey
    val relationship: PropertyKeyRelationship
}