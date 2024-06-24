package com.arbr.og.object_model.common.model

import com.arbr.object_model.core.types.naming.NamedPropertyKey
import com.arbr.object_model.core.types.naming.NamedResourceKey

data class PropertyDetachedIdentifierBase(
    override val resourceKey: NamedResourceKey,
    override val propertyKey: NamedPropertyKey,
    override val relationship: PropertyKeyRelationship,
): PropertyDetachedIdentifier