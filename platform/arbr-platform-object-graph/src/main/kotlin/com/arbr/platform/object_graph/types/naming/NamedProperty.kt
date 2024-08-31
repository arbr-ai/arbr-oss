package com.arbr.platform.object_graph.types.naming

import com.arbr.platform.object_graph.common.model.AttachedPropertyIdentifierBase
import com.arbr.platform.object_graph.common.model.PropertyDetachedIdentifierBase
import com.arbr.platform.object_graph.common.model.PropertyIdentifier
import com.arbr.platform.object_graph.common.model.PropertyKeyRelationship

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
