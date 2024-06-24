package com.arbr.graphql.lang.common

import com.arbr.graphql.lang.node.*

class GraphQlLanguageNodeTransformer {

	fun convertComment(
		node : graphql.language.Comment,
	): GraphQlLanguageNodeComment {
		return GraphQlLanguageNodeComment(
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.content
				?.let { p1 ->
				p1
			},
		)
	}
	
	fun convertDescription(
		node : graphql.language.Description,
	): GraphQlLanguageNodeDescription {
		return GraphQlLanguageNodeDescription(
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.content
				?.let { p1 ->
				p1
			},
			node.multiLine,
		)
	}
	
	fun convertNode(
		node : graphql.language.Node<*>,
	): GraphQlLanguageNodeNode {
		return when (node) {
			is graphql.language.Definition<*> -> convertDefinition(node)
			is graphql.language.AbstractNode<*> -> convertAbstractNode(node)
			is graphql.language.DescribedNode<*> -> convertDescribedNode(node)
			is graphql.language.DirectivesContainer<*> -> convertDirectivesContainer(node)
			is graphql.language.NamedNode<*> -> convertNamedNode(node)
			is graphql.language.Selection<*> -> convertSelection(node)
			is graphql.language.SelectionSetContainer<*> -> convertSelectionSetContainer(node)
			is graphql.language.Type<*> -> convertType(node)
			is graphql.language.Value<*> -> convertValue(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertSourceLocation(
		node : graphql.language.SourceLocation,
	): GraphQlLanguageNodeSourceLocation {
		return GraphQlLanguageNodeSourceLocation(
			node.line,
			node.column,
			node.sourceName
				?.let { p1 ->
				p1
			},
		)
	}
	
	fun convertAbstractNode(
		node : graphql.language.AbstractNode<*>,
	): GraphQlLanguageNodeAbstractNode {
		return when (node) {
			is graphql.language.AbstractDescribedNode<*> -> convertAbstractDescribedNode(node)
			is graphql.language.Argument -> convertArgument(node)
			is graphql.language.ArrayValue -> convertArrayValue(node)
			is graphql.language.BooleanValue -> convertBooleanValue(node)
			is graphql.language.Directive -> convertDirective(node)
			is graphql.language.DirectiveLocation -> convertDirectiveLocation(node)
			is graphql.language.Document -> convertDocument(node)
			is graphql.language.EnumValue -> convertEnumValue(node)
			is graphql.language.Field -> convertField(node)
			is graphql.language.FloatValue -> convertFloatValue(node)
			is graphql.language.FragmentDefinition -> convertFragmentDefinition(node)
			is graphql.language.FragmentSpread -> convertFragmentSpread(node)
			is graphql.language.InlineFragment -> convertInlineFragment(node)
			is graphql.language.IntValue -> convertIntValue(node)
			is graphql.language.ListType -> convertListType(node)
			is graphql.language.NonNullType -> convertNonNullType(node)
			is graphql.language.NullValue -> convertNullValue(node)
			is graphql.language.ObjectField -> convertObjectField(node)
			is graphql.language.ObjectValue -> convertObjectValue(node)
			is graphql.language.OperationDefinition -> convertOperationDefinition(node)
			is graphql.language.OperationTypeDefinition -> convertOperationTypeDefinition(node)
			is graphql.language.SelectionSet -> convertSelectionSet(node)
			is graphql.language.StringValue -> convertStringValue(node)
			is graphql.language.TypeName -> convertTypeName(node)
			is graphql.language.VariableDefinition -> convertVariableDefinition(node)
			is graphql.language.VariableReference -> convertVariableReference(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertDefinition(
		node : graphql.language.Definition<*>,
	): GraphQlLanguageNodeDefinition {
		return when (node) {
			is graphql.language.FragmentDefinition -> convertFragmentDefinition(node)
			is graphql.language.OperationDefinition -> convertOperationDefinition(node)
			is graphql.language.SDLDefinition<*> -> convertSDLDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertDescribedNode(
		node : graphql.language.DescribedNode<*>,
	): GraphQlLanguageNodeDescribedNode {
		return when (node) {
			is graphql.language.AbstractDescribedNode<*> -> convertAbstractDescribedNode(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertDirectivesContainer(
		node : graphql.language.DirectivesContainer<*>,
	): GraphQlLanguageNodeDirectivesContainer {
		return when (node) {
			is graphql.language.EnumTypeDefinition -> convertEnumTypeDefinition(node)
			is graphql.language.EnumValueDefinition -> convertEnumValueDefinition(node)
			is graphql.language.Field -> convertField(node)
			is graphql.language.FieldDefinition -> convertFieldDefinition(node)
			is graphql.language.FragmentDefinition -> convertFragmentDefinition(node)
			is graphql.language.FragmentSpread -> convertFragmentSpread(node)
			is graphql.language.InlineFragment -> convertInlineFragment(node)
			is graphql.language.InputObjectTypeDefinition -> convertInputObjectTypeDefinition(node)
			is graphql.language.InputValueDefinition -> convertInputValueDefinition(node)
			is graphql.language.InterfaceTypeDefinition -> convertInterfaceTypeDefinition(node)
			is graphql.language.ObjectTypeDefinition -> convertObjectTypeDefinition(node)
			is graphql.language.OperationDefinition -> convertOperationDefinition(node)
			is graphql.language.ScalarTypeDefinition -> convertScalarTypeDefinition(node)
			is graphql.language.SchemaDefinition -> convertSchemaDefinition(node)
			is graphql.language.TypeDefinition<*> -> convertTypeDefinition(node)
			is graphql.language.UnionTypeDefinition -> convertUnionTypeDefinition(node)
			is graphql.language.VariableDefinition -> convertVariableDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertNamedNode(
		node : graphql.language.NamedNode<*>,
	): GraphQlLanguageNodeNamedNode {
		return when (node) {
			is graphql.language.Argument -> convertArgument(node)
			is graphql.language.Directive -> convertDirective(node)
			is graphql.language.DirectiveDefinition -> convertDirectiveDefinition(node)
			is graphql.language.DirectiveLocation -> convertDirectiveLocation(node)
			is graphql.language.EnumTypeDefinition -> convertEnumTypeDefinition(node)
			is graphql.language.EnumValue -> convertEnumValue(node)
			is graphql.language.EnumValueDefinition -> convertEnumValueDefinition(node)
			is graphql.language.Field -> convertField(node)
			is graphql.language.FieldDefinition -> convertFieldDefinition(node)
			is graphql.language.FragmentDefinition -> convertFragmentDefinition(node)
			is graphql.language.FragmentSpread -> convertFragmentSpread(node)
			is graphql.language.InputObjectTypeDefinition -> convertInputObjectTypeDefinition(node)
			is graphql.language.InputValueDefinition -> convertInputValueDefinition(node)
			is graphql.language.InterfaceTypeDefinition -> convertInterfaceTypeDefinition(node)
			is graphql.language.ObjectField -> convertObjectField(node)
			is graphql.language.ObjectTypeDefinition -> convertObjectTypeDefinition(node)
			is graphql.language.OperationTypeDefinition -> convertOperationTypeDefinition(node)
			is graphql.language.ScalarTypeDefinition -> convertScalarTypeDefinition(node)
			is graphql.language.TypeDefinition<*> -> convertTypeDefinition(node)
			is graphql.language.TypeName -> convertTypeName(node)
			is graphql.language.UnionTypeDefinition -> convertUnionTypeDefinition(node)
			is graphql.language.VariableDefinition -> convertVariableDefinition(node)
			is graphql.language.VariableReference -> convertVariableReference(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertSelection(
		node : graphql.language.Selection<*>,
	): GraphQlLanguageNodeSelection {
		return when (node) {
			is graphql.language.Field -> convertField(node)
			is graphql.language.FragmentSpread -> convertFragmentSpread(node)
			is graphql.language.InlineFragment -> convertInlineFragment(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertSelectionSetContainer(
		node : graphql.language.SelectionSetContainer<*>,
	): GraphQlLanguageNodeSelectionSetContainer {
		return when (node) {
			is graphql.language.Field -> convertField(node)
			is graphql.language.FragmentDefinition -> convertFragmentDefinition(node)
			is graphql.language.InlineFragment -> convertInlineFragment(node)
			is graphql.language.OperationDefinition -> convertOperationDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertType(
		node : graphql.language.Type<*>,
	): GraphQlLanguageNodeType {
		return when (node) {
			is graphql.language.ListType -> convertListType(node)
			is graphql.language.NonNullType -> convertNonNullType(node)
			is graphql.language.TypeName -> convertTypeName(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertValue(
		node : graphql.language.Value<*>,
	): GraphQlLanguageNodeValue {
		return when (node) {
			is graphql.language.ArrayValue -> convertArrayValue(node)
			is graphql.language.EnumValue -> convertEnumValue(node)
			is graphql.language.NullValue -> convertNullValue(node)
			is graphql.language.ObjectValue -> convertObjectValue(node)
			is graphql.language.ScalarValue<*> -> convertScalarValue(node)
			is graphql.language.VariableReference -> convertVariableReference(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertAbstractDescribedNode(
		node : graphql.language.AbstractDescribedNode<*>,
	): GraphQlLanguageNodeAbstractDescribedNode {
		return when (node) {
			is graphql.language.DirectiveDefinition -> convertDirectiveDefinition(node)
			is graphql.language.EnumTypeDefinition -> convertEnumTypeDefinition(node)
			is graphql.language.EnumValueDefinition -> convertEnumValueDefinition(node)
			is graphql.language.FieldDefinition -> convertFieldDefinition(node)
			is graphql.language.InputObjectTypeDefinition -> convertInputObjectTypeDefinition(node)
			is graphql.language.InputValueDefinition -> convertInputValueDefinition(node)
			is graphql.language.InterfaceTypeDefinition -> convertInterfaceTypeDefinition(node)
			is graphql.language.ObjectTypeDefinition -> convertObjectTypeDefinition(node)
			is graphql.language.ScalarTypeDefinition -> convertScalarTypeDefinition(node)
			is graphql.language.SchemaDefinition -> convertSchemaDefinition(node)
			is graphql.language.UnionTypeDefinition -> convertUnionTypeDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertArgument(
		node : graphql.language.Argument,
	): GraphQlLanguageNodeArgument {
		return GraphQlLanguageNodeArgument(
			node.name
				?.let { p1 ->
				p1
			},
			node.value
				?.let { p1 ->
				convertValue(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertArrayValue(
		node : graphql.language.ArrayValue,
	): GraphQlLanguageNodeArrayValue {
		return GraphQlLanguageNodeArrayValue(
			node.values
				?.let { p1 ->
				p1
					.map { p2 ->
					convertValue(p2)
				}
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertDirective(
		node : graphql.language.Directive,
	): GraphQlLanguageNodeDirective {
		return GraphQlLanguageNodeDirective(
			node.arguments
				?.let { p1 ->
				p1
					.map { p2 ->
					convertArgument(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertDirectiveLocation(
		node : graphql.language.DirectiveLocation,
	): GraphQlLanguageNodeDirectiveLocation {
		return GraphQlLanguageNodeDirectiveLocation(
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertDocument(
		node : graphql.language.Document,
	): GraphQlLanguageNodeDocument {
		return GraphQlLanguageNodeDocument(
			node.definitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDefinition(p2)
				}
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertEnumValue(
		node : graphql.language.EnumValue,
	): GraphQlLanguageNodeEnumValue {
		return GraphQlLanguageNodeEnumValue(
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertField(
		node : graphql.language.Field,
	): GraphQlLanguageNodeField {
		return GraphQlLanguageNodeField(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.selectionSet
				?.let { p1 ->
				convertSelectionSet(p1)
			},
			node.arguments
				?.let { p1 ->
				p1
					.map { p2 ->
					convertArgument(p2)
				}
			},
			node.resultKey
				?.let { p1 ->
				p1
			},
			node.alias
				?.let { p1 ->
				p1
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertFragmentDefinition(
		node : graphql.language.FragmentDefinition,
	): GraphQlLanguageNodeFragmentDefinition {
		return GraphQlLanguageNodeFragmentDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.selectionSet
				?.let { p1 ->
				convertSelectionSet(p1)
			},
			node.typeCondition
				?.let { p1 ->
				convertTypeName(p1)
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertFragmentSpread(
		node : graphql.language.FragmentSpread,
	): GraphQlLanguageNodeFragmentSpread {
		return GraphQlLanguageNodeFragmentSpread(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertInlineFragment(
		node : graphql.language.InlineFragment,
	): GraphQlLanguageNodeInlineFragment {
		return GraphQlLanguageNodeInlineFragment(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.selectionSet
				?.let { p1 ->
				convertSelectionSet(p1)
			},
			node.typeCondition
				?.let { p1 ->
				convertTypeName(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertListType(
		node : graphql.language.ListType,
	): GraphQlLanguageNodeListType {
		return GraphQlLanguageNodeListType(
			node.type
				?.let { p1 ->
				convertType(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertNonNullType(
		node : graphql.language.NonNullType,
	): GraphQlLanguageNodeNonNullType {
		return GraphQlLanguageNodeNonNullType(
			node.type
				?.let { p1 ->
				convertType(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertNullValue(
		node : graphql.language.NullValue,
	): GraphQlLanguageNodeNullValue {
		return GraphQlLanguageNodeNullValue(
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertObjectField(
		node : graphql.language.ObjectField,
	): GraphQlLanguageNodeObjectField {
		return GraphQlLanguageNodeObjectField(
			node.name
				?.let { p1 ->
				p1
			},
			node.value
				?.let { p1 ->
				convertValue(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertObjectValue(
		node : graphql.language.ObjectValue,
	): GraphQlLanguageNodeObjectValue {
		return GraphQlLanguageNodeObjectValue(
			node.objectFields
				?.let { p1 ->
				p1
					.map { p2 ->
					convertObjectField(p2)
				}
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertOperationDefinition(
		node : graphql.language.OperationDefinition,
	): GraphQlLanguageNodeOperationDefinition {
		return GraphQlLanguageNodeOperationDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.operation
				?.let { p1 ->
				com.arbr.graphql.lang.common.GraphQlOperationValue.fromLangValue(p1)
			},
			node.variableDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertVariableDefinition(p2)
				}
			},
			node.selectionSet
				?.let { p1 ->
				convertSelectionSet(p1)
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertOperationTypeDefinition(
		node : graphql.language.OperationTypeDefinition,
	): GraphQlLanguageNodeOperationTypeDefinition {
		return GraphQlLanguageNodeOperationTypeDefinition(
			node.name
				?.let { p1 ->
				p1
			},
			node.typeName
				?.let { p1 ->
				convertTypeName(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertSDLDefinition(
		node : graphql.language.SDLDefinition<*>,
	): GraphQlLanguageNodeSDLDefinition {
		return when (node) {
			is graphql.language.SDLNamedDefinition<*> -> convertSDLNamedDefinition(node)
			is graphql.language.SchemaDefinition -> convertSchemaDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertScalarValue(
		node : graphql.language.ScalarValue<*>,
	): GraphQlLanguageNodeScalarValue {
		return when (node) {
			is graphql.language.BooleanValue -> convertBooleanValue(node)
			is graphql.language.FloatValue -> convertFloatValue(node)
			is graphql.language.IntValue -> convertIntValue(node)
			is graphql.language.StringValue -> convertStringValue(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertSelectionSet(
		node : graphql.language.SelectionSet,
	): GraphQlLanguageNodeSelectionSet {
		return GraphQlLanguageNodeSelectionSet(
			node.selections
				?.let { p1 ->
				p1
					.map { p2 ->
					convertSelection(p2)
				}
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertTypeName(
		node : graphql.language.TypeName,
	): GraphQlLanguageNodeTypeName {
		return GraphQlLanguageNodeTypeName(
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertVariableDefinition(
		node : graphql.language.VariableDefinition,
	): GraphQlLanguageNodeVariableDefinition {
		return GraphQlLanguageNodeVariableDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.type
				?.let { p1 ->
				convertType(p1)
			},
			node.defaultValue
				?.let { p1 ->
				convertValue(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertVariableReference(
		node : graphql.language.VariableReference,
	): GraphQlLanguageNodeVariableReference {
		return GraphQlLanguageNodeVariableReference(
			node.name
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertBooleanValue(
		node : graphql.language.BooleanValue,
	): GraphQlLanguageNodeBooleanValue {
		return GraphQlLanguageNodeBooleanValue(
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertEnumValueDefinition(
		node : graphql.language.EnumValueDefinition,
	): GraphQlLanguageNodeEnumValueDefinition {
		return GraphQlLanguageNodeEnumValueDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertFieldDefinition(
		node : graphql.language.FieldDefinition,
	): GraphQlLanguageNodeFieldDefinition {
		return GraphQlLanguageNodeFieldDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.inputValueDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertInputValueDefinition(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.type
				?.let { p1 ->
				convertType(p1)
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertFloatValue(
		node : graphql.language.FloatValue,
	): GraphQlLanguageNodeFloatValue {
		return GraphQlLanguageNodeFloatValue(
			node.value
				?.let { p1 ->
				p1.toDouble()
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertInputValueDefinition(
		node : graphql.language.InputValueDefinition,
	): GraphQlLanguageNodeInputValueDefinition {
		return GraphQlLanguageNodeInputValueDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.type
				?.let { p1 ->
				convertType(p1)
			},
			node.defaultValue
				?.let { p1 ->
				convertValue(p1)
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertIntValue(
		node : graphql.language.IntValue,
	): GraphQlLanguageNodeIntValue {
		return GraphQlLanguageNodeIntValue(
			node.value
				?.let { p1 ->
				p1.toInt()
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertSDLNamedDefinition(
		node : graphql.language.SDLNamedDefinition<*>,
	): GraphQlLanguageNodeSDLNamedDefinition {
		return when (node) {
			is graphql.language.DirectiveDefinition -> convertDirectiveDefinition(node)
			is graphql.language.TypeDefinition<*> -> convertTypeDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertSchemaDefinition(
		node : graphql.language.SchemaDefinition,
	): GraphQlLanguageNodeSchemaDefinition {
		return when (node) {
			is graphql.language.SchemaExtensionDefinition -> convertSchemaExtensionDefinition(node)
			else -> convertSchemaDefinitionProper(node)
		}
		
	}
	
	fun convertSchemaDefinitionProper(
		node : graphql.language.SchemaDefinition,
	): GraphQlLanguageNodeSchemaDefinition {
		return GraphQlLanguageNodeSchemaDefinitionProper(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.operationTypeDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertOperationTypeDefinition(p2)
				}
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertStringValue(
		node : graphql.language.StringValue,
	): GraphQlLanguageNodeStringValue {
		return GraphQlLanguageNodeStringValue(
			node.value
				?.let { p1 ->
				p1
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertDirectiveDefinition(
		node : graphql.language.DirectiveDefinition,
	): GraphQlLanguageNodeDirectiveDefinition {
		return GraphQlLanguageNodeDirectiveDefinition(
			node.inputValueDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertInputValueDefinition(p2)
				}
			},
			node.directiveLocations
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirectiveLocation(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertSchemaExtensionDefinition(
		node : graphql.language.SchemaExtensionDefinition,
	): GraphQlLanguageNodeSchemaExtensionDefinition {
		return GraphQlLanguageNodeSchemaExtensionDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.operationTypeDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertOperationTypeDefinition(p2)
				}
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertTypeDefinition(
		node : graphql.language.TypeDefinition<*>,
	): GraphQlLanguageNodeTypeDefinition {
		return when (node) {
			is graphql.language.EnumTypeDefinition -> convertEnumTypeDefinition(node)
			is graphql.language.ImplementingTypeDefinition<*> -> convertImplementingTypeDefinition(node)
			is graphql.language.InputObjectTypeDefinition -> convertInputObjectTypeDefinition(node)
			is graphql.language.ScalarTypeDefinition -> convertScalarTypeDefinition(node)
			is graphql.language.UnionTypeDefinition -> convertUnionTypeDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertEnumTypeDefinition(
		node : graphql.language.EnumTypeDefinition,
	): GraphQlLanguageNodeEnumTypeDefinition {
		return when (node) {
			is graphql.language.EnumTypeExtensionDefinition -> convertEnumTypeExtensionDefinition(node)
			else -> convertEnumTypeDefinitionProper(node)
		}
		
	}
	
	fun convertEnumTypeDefinitionProper(
		node : graphql.language.EnumTypeDefinition,
	): GraphQlLanguageNodeEnumTypeDefinition {
		return GraphQlLanguageNodeEnumTypeDefinitionProper(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.enumValueDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertEnumValueDefinition(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertImplementingTypeDefinition(
		node : graphql.language.ImplementingTypeDefinition<*>,
	): GraphQlLanguageNodeImplementingTypeDefinition {
		return when (node) {
			is graphql.language.InterfaceTypeDefinition -> convertInterfaceTypeDefinition(node)
			is graphql.language.ObjectTypeDefinition -> convertObjectTypeDefinition(node)
			else -> throw IllegalStateException("Unrecognized subclass case")
		}
		
	}
	
	fun convertInputObjectTypeDefinition(
		node : graphql.language.InputObjectTypeDefinition,
	): GraphQlLanguageNodeInputObjectTypeDefinition {
		return when (node) {
			is graphql.language.InputObjectTypeExtensionDefinition -> convertInputObjectTypeExtensionDefinition(node)
			else -> convertInputObjectTypeDefinitionProper(node)
		}
		
	}
	
	fun convertInputObjectTypeDefinitionProper(
		node : graphql.language.InputObjectTypeDefinition,
	): GraphQlLanguageNodeInputObjectTypeDefinition {
		return GraphQlLanguageNodeInputObjectTypeDefinitionProper(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.inputValueDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertInputValueDefinition(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertScalarTypeDefinition(
		node : graphql.language.ScalarTypeDefinition,
	): GraphQlLanguageNodeScalarTypeDefinition {
		return when (node) {
			is graphql.language.ScalarTypeExtensionDefinition -> convertScalarTypeExtensionDefinition(node)
			else -> convertScalarTypeDefinitionProper(node)
		}
		
	}
	
	fun convertScalarTypeDefinitionProper(
		node : graphql.language.ScalarTypeDefinition,
	): GraphQlLanguageNodeScalarTypeDefinition {
		return GraphQlLanguageNodeScalarTypeDefinitionProper(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertUnionTypeDefinition(
		node : graphql.language.UnionTypeDefinition,
	): GraphQlLanguageNodeUnionTypeDefinition {
		return when (node) {
			is graphql.language.UnionTypeExtensionDefinition -> convertUnionTypeExtensionDefinition(node)
			else -> convertUnionTypeDefinitionProper(node)
		}
		
	}
	
	fun convertUnionTypeDefinitionProper(
		node : graphql.language.UnionTypeDefinition,
	): GraphQlLanguageNodeUnionTypeDefinition {
		return GraphQlLanguageNodeUnionTypeDefinitionProper(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.memberTypes
				?.let { p1 ->
				p1
					.map { p2 ->
					convertType(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertEnumTypeExtensionDefinition(
		node : graphql.language.EnumTypeExtensionDefinition,
	): GraphQlLanguageNodeEnumTypeExtensionDefinition {
		return GraphQlLanguageNodeEnumTypeExtensionDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.enumValueDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertEnumValueDefinition(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertInputObjectTypeExtensionDefinition(
		node : graphql.language.InputObjectTypeExtensionDefinition,
	): GraphQlLanguageNodeInputObjectTypeExtensionDefinition {
		return GraphQlLanguageNodeInputObjectTypeExtensionDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.inputValueDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertInputValueDefinition(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertInterfaceTypeDefinition(
		node : graphql.language.InterfaceTypeDefinition,
	): GraphQlLanguageNodeInterfaceTypeDefinition {
		return when (node) {
			is graphql.language.InterfaceTypeExtensionDefinition -> convertInterfaceTypeExtensionDefinition(node)
			else -> convertInterfaceTypeDefinitionProper(node)
		}
		
	}
	
	fun convertInterfaceTypeDefinitionProper(
		node : graphql.language.InterfaceTypeDefinition,
	): GraphQlLanguageNodeInterfaceTypeDefinition {
		return GraphQlLanguageNodeInterfaceTypeDefinitionProper(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.fieldDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertFieldDefinition(p2)
				}
			},
			node.implements
				?.let { p1 ->
				p1
					.map { p2 ->
					convertType(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertObjectTypeDefinition(
		node : graphql.language.ObjectTypeDefinition,
	): GraphQlLanguageNodeObjectTypeDefinition {
		return when (node) {
			is graphql.language.ObjectTypeExtensionDefinition -> convertObjectTypeExtensionDefinition(node)
			else -> convertObjectTypeDefinitionProper(node)
		}
		
	}
	
	fun convertObjectTypeDefinitionProper(
		node : graphql.language.ObjectTypeDefinition,
	): GraphQlLanguageNodeObjectTypeDefinition {
		return GraphQlLanguageNodeObjectTypeDefinitionProper(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.fieldDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertFieldDefinition(p2)
				}
			},
			node.implements
				?.let { p1 ->
				p1
					.map { p2 ->
					convertType(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertScalarTypeExtensionDefinition(
		node : graphql.language.ScalarTypeExtensionDefinition,
	): GraphQlLanguageNodeScalarTypeExtensionDefinition {
		return GraphQlLanguageNodeScalarTypeExtensionDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertUnionTypeExtensionDefinition(
		node : graphql.language.UnionTypeExtensionDefinition,
	): GraphQlLanguageNodeUnionTypeExtensionDefinition {
		return GraphQlLanguageNodeUnionTypeExtensionDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.memberTypes
				?.let { p1 ->
				p1
					.map { p2 ->
					convertType(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertInterfaceTypeExtensionDefinition(
		node : graphql.language.InterfaceTypeExtensionDefinition,
	): GraphQlLanguageNodeInterfaceTypeExtensionDefinition {
		return GraphQlLanguageNodeInterfaceTypeExtensionDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.fieldDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertFieldDefinition(p2)
				}
			},
			node.implements
				?.let { p1 ->
				p1
					.map { p2 ->
					convertType(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
	fun convertObjectTypeExtensionDefinition(
		node : graphql.language.ObjectTypeExtensionDefinition,
	): GraphQlLanguageNodeObjectTypeExtensionDefinition {
		return GraphQlLanguageNodeObjectTypeExtensionDefinition(
			node.directives
				?.let { p1 ->
				p1
					.map { p2 ->
					convertDirective(p2)
				}
			},
			node.fieldDefinitions
				?.let { p1 ->
				p1
					.map { p2 ->
					convertFieldDefinition(p2)
				}
			},
			node.implements
				?.let { p1 ->
				p1
					.map { p2 ->
					convertType(p2)
				}
			},
			node.name
				?.let { p1 ->
				p1
			},
			node.description
				?.let { p1 ->
				convertDescription(p1)
			},
			node.sourceLocation
				?.let { p1 ->
				convertSourceLocation(p1)
			},
			node.comments
				.map { p1 ->
				convertComment(p1)
			},
			node.additionalData,
		)
	}
	
}
