package com.arbr.graphql.lang.node

data class GraphQlLanguageNodeDirectiveDefinition(
	val inputValueDefinitions: List<GraphQlLanguageNodeInputValueDefinition>?,
	val directiveLocations: List<GraphQlLanguageNodeDirectiveLocation>?,
	override val name: String?,
	override val description: GraphQlLanguageNodeDescription?,
	override val sourceLocation: GraphQlLanguageNodeSourceLocation?,
	override val comments: List<GraphQlLanguageNodeComment>,
	override val additionalData: Map<String, String>,
): GraphQlLanguageNodeAbstractDescribedNode, GraphQlLanguageNodeSDLNamedDefinition, GraphQlLanguageNodeNamedNode