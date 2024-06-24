package com.arbr.object_model.core.types.naming

interface NamedSchema<
        SchemaKeyType : NamedSchemaKey,
        ResourceKeyType : NamedResourceKey,
        PropertyKeyType : NamedPropertyKey
        > {
    val schemaKey: SchemaKeyType
}
