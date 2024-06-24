package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeNonNullType(
	val type: GraphQlLanguageNodeType?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeType