package com.arbr.platform.object_graph.types.naming

interface NamedSchema<
        SchemaKeyType : NamedSchemaKey,
        ResourceKeyType : NamedResourceKey,
        PropertyKeyType : NamedPropertyKey
        > {
    val schemaKey: SchemaKeyType
}
