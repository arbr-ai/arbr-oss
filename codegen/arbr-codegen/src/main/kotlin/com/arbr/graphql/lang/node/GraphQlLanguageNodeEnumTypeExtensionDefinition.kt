package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeEnumTypeExtensionDefinition(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val enumValueDefinitions: List<GraphQlLanguageNodeEnumValueDefinition>?,
	override val name: String?,
	override val description: GraphQlLanguageNodeDescription?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeEnumTypeDefinition