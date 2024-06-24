package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeObjectValue(
	val objectFields: List<GraphQlLanguageNodeObjectField>?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeValue