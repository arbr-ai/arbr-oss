package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeDescription(
	val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	val content: String?,
	val multiLine: Boolean,
): GraphQlLanguageNodeBase