package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeInputObjectTypeExtensionDefinition(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val inputValueDefinitions: List<GraphQlLanguageNodeInputValueDefinition>?,
	override val name: String?,
	override val description: GraphQlLanguageNodeDescription?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeInputObjectTypeDefinition