package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeField(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val selectionSet: GraphQlLanguageNodeSelectionSet?,
	val arguments: List<GraphQlLanguageNodeArgument>?,
	val resultKey: String?,
	val alias: String?,
	override val name: String?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeSelection, GraphQlLanguageNodeSelectionSetContainer, GraphQlLanguageNodeDirectivesContainer, GraphQlLanguageNodeNamedNode