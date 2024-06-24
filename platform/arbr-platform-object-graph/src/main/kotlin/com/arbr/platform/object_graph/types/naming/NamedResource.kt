package com.arbr.object_model.core.types.naming

interface NamedResource<
        SchemaKeyType : NamedSchemaKey,
        ResourceKeyType : NamedResourceKey,
        PropertyKeyType : NamedPropertyKey,
        SchemaType : NamedSchema<SchemaKeyType, ResourceKeyType, PropertyKeyType>,
        > {
    val schemaKey: SchemaKeyType
    val resourceKey: ResourceKeyType
}
