package com.arbr.object_model.core.types.naming

import com.arbr.og.object_model.common.model.AttachedPropertyIdentifierBase
import com.arbr.og.object_model.common.model.PropertyDetachedIdentifierBase
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.og.object_model.common.model.PropertyKeyRelationship

@Suppress("MemberVisibilityCanBePrivate")
abstract class NamedProperty<
        SchemaKeyType : NamedSchemaKey,
        ResourceKeyType : NamedResourceKey,
        PropertyKeyType : NamedPropertyKey,
        SchemaType : NamedSchema<SchemaKeyType, ResourceKeyType, PropertyKeyType>,
        ResourceType : NamedResource<SchemaKeyType, ResourceKeyType, PropertyKeyType, SchemaType>,
        >(
    val schemaKey: SchemaKeyType,
    val resourceKey: ResourceKeyType,
    val propertyKey: PropertyKeyType,
    val relationship: PropertyKeyRelationship,
) {
    val detachedIdentifier = PropertyDetachedIdentifierBase(
        resourceKey = resourceKey,
        propertyKey = propertyKey,
        relationship,
    )

    fun attachedIdentifier(
        resourceUuid: String,
    ): PropertyIdentifier {
        return AttachedPropertyIdentifierBase(
            detachedIdentifier,
            resourceUuid,
        )
    }
}
