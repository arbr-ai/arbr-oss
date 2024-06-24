package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeSchemaDefinition: GraphQlLanguageNodeAbstractDescribedNode, GraphQlLanguageNodeSDLDefinition, GraphQlLanguageNodeDirectivesContainer {
	// override val directives: List<GraphQlLanguageNodeDirective>?
	// override val description: GraphQlLanguageNodeDescription?
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	
	val operationTypeDefinitions: List<GraphQlLanguageNodeOperationTypeDefinition>?
}