package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeSourceLocation(
	val line: Int,
	val column: Int,
	val sourceName: String?,
): GraphQlLanguageNodeBase