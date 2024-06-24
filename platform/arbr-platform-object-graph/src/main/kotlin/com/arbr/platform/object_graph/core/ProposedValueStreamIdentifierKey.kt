package com.arbr.platform.object_graph.core

data class ProposedValueStreamIdentifierKey(
    val parentUuid: String,
    val propertyName: String,
    val relationship: String,
    val resourceType: String,
)