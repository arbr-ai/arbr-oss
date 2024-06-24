package com.arbr.graphql.converter.model

internal data class MappedNodeFieldModel(
    val name: String,
    /**
     * Type name not considering nullability marker
     */
    val typeName: String,
    val nullable: Boolean,
    val isOverride: Boolean,
)