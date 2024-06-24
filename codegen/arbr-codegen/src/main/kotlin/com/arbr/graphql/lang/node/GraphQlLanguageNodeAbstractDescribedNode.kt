package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeAbstractDescribedNode: GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeDescribedNode {
	// override val description: GraphQlLanguageNodeDescription?
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	
}