package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeObjectField(
	override val name: String?,
	val value: GraphQlLanguageNodeValue?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeNamedNode