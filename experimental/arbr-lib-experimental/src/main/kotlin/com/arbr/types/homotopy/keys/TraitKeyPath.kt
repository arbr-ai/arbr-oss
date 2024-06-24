package com.arbr.types.homotopy.keys

import com.arbr.types.homotopy.htype.NodeHType

/**
 * A descendent trait related by a compound key path
 * Trait is non-null to reflect the opinion that the down-path after a node that doesn't amount to a relation among
 * traited types is irrelevant
 */
data class TraitKeyPath<Tr, F: Tr>(
    val keyPath: List<KeyPathTokenKind>,
    val traitHType: NodeHType<Tr, F>,
)