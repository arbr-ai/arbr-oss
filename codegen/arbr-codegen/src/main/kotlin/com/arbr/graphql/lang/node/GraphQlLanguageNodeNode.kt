package com.arbr.graphql.lang.node

sealed interface GraphQlLanguageNodeNode: GraphQlLanguageNodeBase {
	val sourceLocation: GraphQlLanguageNodeSourceLocation?
	val comments: List<GraphQlLanguageNodeComment>
	val additionalData: Map<String, String>
}