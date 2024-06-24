package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeUnionTypeDefinitionProper(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val memberTypes: List<GraphQlLanguageNodeType>?,
	override val name: String?,
	override val description: GraphQlLanguageNodeDescription?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeUnionTypeDefinition