package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeInlineFragment(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val selectionSet: GraphQlLanguageNodeSelectionSet?,
	val typeCondition: GraphQlLanguageNodeTypeName?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeSelection, GraphQlLanguageNodeSelectionSetContainer, GraphQlLanguageNodeDirectivesContainer