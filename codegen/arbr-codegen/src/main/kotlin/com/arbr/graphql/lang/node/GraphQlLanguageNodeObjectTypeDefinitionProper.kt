package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeObjectTypeDefinitionProper(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val fieldDefinitions: List<GraphQlLanguageNodeFieldDefinition>?,
	override val implements: List<GraphQlLanguageNodeType>?,
	override val name: String?,
	override val description: GraphQlLanguageNodeDescription?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeObjectTypeDefinition