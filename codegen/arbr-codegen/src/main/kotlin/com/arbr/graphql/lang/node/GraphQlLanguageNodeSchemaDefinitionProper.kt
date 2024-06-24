package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeSchemaDefinitionProper(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val description: GraphQlLanguageNodeDescription?,
	override val operationTypeDefinitions: List<GraphQlLanguageNodeOperationTypeDefinition>?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeSchemaDefinition