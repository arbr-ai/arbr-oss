package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeDocument(
	val definitions: List<GraphQlLanguageNodeDefinition>?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode