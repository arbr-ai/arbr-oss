package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeArrayValue(
	val values: List<GraphQlLanguageNodeValue>?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeValue