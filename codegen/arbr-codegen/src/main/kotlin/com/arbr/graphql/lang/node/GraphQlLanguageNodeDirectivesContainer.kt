package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeDirectivesContainer: GraphQlLanguageNodeNode {
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	
	val directives: List<GraphQlLanguageNodeDirective>?
}