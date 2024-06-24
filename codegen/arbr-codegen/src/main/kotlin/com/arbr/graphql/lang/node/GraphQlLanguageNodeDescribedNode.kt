package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeDescribedNode: GraphQlLanguageNodeNode {
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	
	val description: GraphQlLanguageNodeDescription?
}