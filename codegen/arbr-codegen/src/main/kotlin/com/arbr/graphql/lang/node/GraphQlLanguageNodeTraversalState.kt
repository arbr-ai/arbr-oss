package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeTraversalState(
    val node: GraphQlLanguageNodeBase,
    val context: GraphQlLanguageNodeTraversalContext,
)