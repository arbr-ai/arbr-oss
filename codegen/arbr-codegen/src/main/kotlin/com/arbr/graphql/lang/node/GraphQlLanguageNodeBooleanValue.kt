package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeBooleanValue(
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeScalarValue