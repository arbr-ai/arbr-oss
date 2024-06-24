package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeUnionTypeDefinition: GraphQlLanguageNodeAbstractDescribedNode, GraphQlLanguageNodeTypeDefinition, GraphQlLanguageNodeDirectivesContainer, GraphQlLanguageNodeNamedNode {
	// override val directives: List<GraphQlLanguageNodeDirective>?
	// override val name: String?
	// override val description: GraphQlLanguageNodeDescription?
	// override val sourceLocation: GraphQlLanguageNodeSourceLocation?
	// override val comments: List<GraphQlLanguageNodeComment>
	// override val additionalData: Map<String, String>
	
	val memberTypes: List<GraphQlLanguageNodeType>?
}