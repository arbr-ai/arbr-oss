package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeNamedNode: GraphQlLanguageNodeNode {
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	
	val name: String?
}