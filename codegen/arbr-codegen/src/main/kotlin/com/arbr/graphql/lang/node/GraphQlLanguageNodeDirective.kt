package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeDirective(
	val arguments: List<GraphQlLanguageNodeArgument>?,
	override val name: String?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeNamedNode