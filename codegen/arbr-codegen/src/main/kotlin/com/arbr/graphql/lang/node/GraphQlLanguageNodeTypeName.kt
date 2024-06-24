package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeTypeName(
	override val name: String?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeType, GraphQlLanguageNodeNamedNode