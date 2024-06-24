package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeComment(
	val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	val content: String?,
): GraphQlLanguageNodeBase