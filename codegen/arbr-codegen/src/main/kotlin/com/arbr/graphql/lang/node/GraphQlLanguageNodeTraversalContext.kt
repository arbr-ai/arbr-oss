package com.arbr.graphql.lang.node

import com.arbr.graphql.lang.common.GraphQlSimpleNodeType

data class GraphQlLanguageNodeTraversalContext(
    val nodeType: GraphQlSimpleNodeType,
    val nodeId: String,
    val phase: GraphQlLanguageNodeTraversalContextPhase,
    val parentNodeId: String?,
)