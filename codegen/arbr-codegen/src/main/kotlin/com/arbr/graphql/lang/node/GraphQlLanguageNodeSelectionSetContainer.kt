package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeSelectionSetContainer: GraphQlLanguageNodeNode {
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	
	val selectionSet: GraphQlLanguageNodeSelectionSet?
}