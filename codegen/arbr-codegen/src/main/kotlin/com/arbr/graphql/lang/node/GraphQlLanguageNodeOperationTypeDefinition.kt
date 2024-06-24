package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeOperationTypeDefinition(
	override val name: String?,
	val typeName: GraphQlLanguageNodeTypeName?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeNamedNode