package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeVariableDefinition(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val name: String?,
	val type: GraphQlLanguageNodeType?,
	val defaultValue: GraphQlLanguageNodeValue?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeDirectivesContainer, GraphQlLanguageNodeNamedNode