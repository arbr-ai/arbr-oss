package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeOperationDefinition(
	override val directives: List<GraphQlLanguageNodeDirective>?,
	val operation: com.arbr.graphql.lang.common.GraphQlOperationValue?,
	val variableDefinitions: List<GraphQlLanguageNodeVariableDefinition>?,
	override val selectionSet: GraphQlLanguageNodeSelectionSet?,
	val name: String?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractNode, GraphQlLanguageNodeDefinition, GraphQlLanguageNodeSelectionSetContainer, GraphQlLanguageNodeDirectivesContainer