package com.arbr.platform.object_graph.common.model

/**
 * An immutable view into a proposed value stream.
 */
data class ProposedValue<S : Any>(
    val identifier: PropertyIdentifier,
    val value: S?,
)