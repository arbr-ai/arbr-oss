package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeFragmentDefinition(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	override val selectionSet: GraphQlLanguageNodeSelectionSet?,
	val typeCondition: GraphQlLanguageNodeTypeName?,
	override val name: String?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeDefinition, GraphQlLanguageNodeSelectionSetContainer, GraphQlLanguageNodeDirectivesContainer, GraphQlLanguageNodeNamedNode