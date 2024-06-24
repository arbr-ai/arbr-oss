package com.arbr.og.object_model.common.model

import com.arbr.object_model.core.types.naming.NamedPropertyKey
import com.arbr.object_model.core.types.naming.NamedResourceKey

data class AttachedPropertyIdentifierBase(
    val propertyDetachedIdentifier: PropertyDetachedIdentifier,
    override val resourceUuid: String,
): PropertyIdentifier {
    override val resourceKey: NamedResourceKey = propertyDetachedIdentifier.resourceKey
    override val propertyKey: NamedPropertyKey = propertyDetachedIdentifier.propertyKey
    override val relationship: PropertyKeyRelationship = propertyDetachedIdentifier.relationship

    override fun toString(): String {
        return "${resourceKey}[$resourceUuid].${propertyKey}:${relationship.name}"
    }
}

