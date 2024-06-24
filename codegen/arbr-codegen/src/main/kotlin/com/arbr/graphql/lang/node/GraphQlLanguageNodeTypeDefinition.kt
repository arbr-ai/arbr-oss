package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeTypeDefinition: GraphQlLanguageNodeSDLNamedDefinition, GraphQlLanguageNodeDirectivesContainer, GraphQlLanguageNodeNamedNode {
	// override val name: String?
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	// override val directives: List<GraphQlLanguageNodeDirective>?
	
}