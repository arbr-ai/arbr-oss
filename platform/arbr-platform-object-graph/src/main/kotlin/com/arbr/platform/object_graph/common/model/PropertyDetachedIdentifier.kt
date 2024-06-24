package com.arbr.og.object_model.common.model

import com.arbr.object_model.core.types.naming.NamedPropertyKey
import com.arbr.object_model.core.types.naming.NamedResourceKey

/**
 * Property "slot" identifier, independent of the particular resource
 */
interface PropertyDetachedIdentifier {
    val resourceKey: NamedResourceKey
    val propertyKey: NamedPropertyKey
    val relationship: PropertyKeyRelationship
}