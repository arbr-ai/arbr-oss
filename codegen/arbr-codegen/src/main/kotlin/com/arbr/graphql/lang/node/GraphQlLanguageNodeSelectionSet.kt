package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeSelectionSet(
	val selections: List<GraphQlLanguageNodeSelection>?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode