package com.arbr.graphql.lang.common

import com.arbr.graphql.lang.node.*

typealias AppendableBlock = (Appendable.() -> Unit)

abstract class GraphQlNodeRenderer: GraphQlNodeProcessor {

    private val touchedNodeIds = mutableSetOf<String>()

	open fun renderGraphQlLanguageNodeInterfaceTypeDefinitionProper(
		node : GraphQlLanguageNodeInterfaceTypeDefinitionProper,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeInterfaceTypeExtensionDefinition(
		node : GraphQlLanguageNodeInterfaceTypeExtensionDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeObjectTypeDefinitionProper(
		node : GraphQlLanguageNodeObjectTypeDefinitionProper,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeObjectTypeExtensionDefinition(
		node : GraphQlLanguageNodeObjectTypeExtensionDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeEnumTypeDefinitionProper(
		node : GraphQlLanguageNodeEnumTypeDefinitionProper,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeEnumTypeExtensionDefinition(
		node : GraphQlLanguageNodeEnumTypeExtensionDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeInputObjectTypeDefinitionProper(
		node : GraphQlLanguageNodeInputObjectTypeDefinitionProper,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeInputObjectTypeExtensionDefinition(
		node : GraphQlLanguageNodeInputObjectTypeExtensionDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeInterfaceTypeDefinition(
		node : GraphQlLanguageNodeInterfaceTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeObjectTypeDefinition(
		node : GraphQlLanguageNodeObjectTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeScalarTypeDefinitionProper(
		node : GraphQlLanguageNodeScalarTypeDefinitionProper,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeScalarTypeExtensionDefinition(
		node : GraphQlLanguageNodeScalarTypeExtensionDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeUnionTypeDefinitionProper(
		node : GraphQlLanguageNodeUnionTypeDefinitionProper,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeUnionTypeExtensionDefinition(
		node : GraphQlLanguageNodeUnionTypeExtensionDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeEnumTypeDefinition(
		node : GraphQlLanguageNodeEnumTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeImplementingTypeDefinition(
		node : GraphQlLanguageNodeImplementingTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeInputObjectTypeDefinition(
		node : GraphQlLanguageNodeInputObjectTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeScalarTypeDefinition(
		node : GraphQlLanguageNodeScalarTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeUnionTypeDefinition(
		node : GraphQlLanguageNodeUnionTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDirectiveDefinition(
		node : GraphQlLanguageNodeDirectiveDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSchemaDefinitionProper(
		node : GraphQlLanguageNodeSchemaDefinitionProper,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSchemaExtensionDefinition(
		node : GraphQlLanguageNodeSchemaExtensionDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeTypeDefinition(
		node : GraphQlLanguageNodeTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeBooleanValue(
		node : GraphQlLanguageNodeBooleanValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeEnumValueDefinition(
		node : GraphQlLanguageNodeEnumValueDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeFieldDefinition(
		node : GraphQlLanguageNodeFieldDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeFloatValue(
		node : GraphQlLanguageNodeFloatValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeInputValueDefinition(
		node : GraphQlLanguageNodeInputValueDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeIntValue(
		node : GraphQlLanguageNodeIntValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSDLNamedDefinition(
		node : GraphQlLanguageNodeSDLNamedDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSchemaDefinition(
		node : GraphQlLanguageNodeSchemaDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeStringValue(
		node : GraphQlLanguageNodeStringValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeAbstractDescribedNode(
		node : GraphQlLanguageNodeAbstractDescribedNode,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeArgument(
		node : GraphQlLanguageNodeArgument,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeArrayValue(
		node : GraphQlLanguageNodeArrayValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDirective(
		node : GraphQlLanguageNodeDirective,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDirectiveLocation(
		node : GraphQlLanguageNodeDirectiveLocation,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDocument(
		node : GraphQlLanguageNodeDocument,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeEnumValue(
		node : GraphQlLanguageNodeEnumValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeField(
		node : GraphQlLanguageNodeField,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeFragmentDefinition(
		node : GraphQlLanguageNodeFragmentDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeFragmentSpread(
		node : GraphQlLanguageNodeFragmentSpread,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeInlineFragment(
		node : GraphQlLanguageNodeInlineFragment,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeListType(
		node : GraphQlLanguageNodeListType,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeNonNullType(
		node : GraphQlLanguageNodeNonNullType,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeNullValue(
		node : GraphQlLanguageNodeNullValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeObjectField(
		node : GraphQlLanguageNodeObjectField,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeObjectValue(
		node : GraphQlLanguageNodeObjectValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeOperationDefinition(
		node : GraphQlLanguageNodeOperationDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeOperationTypeDefinition(
		node : GraphQlLanguageNodeOperationTypeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSDLDefinition(
		node : GraphQlLanguageNodeSDLDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeScalarValue(
		node : GraphQlLanguageNodeScalarValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSelectionSet(
		node : GraphQlLanguageNodeSelectionSet,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeTypeName(
		node : GraphQlLanguageNodeTypeName,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeVariableDefinition(
		node : GraphQlLanguageNodeVariableDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeVariableReference(
		node : GraphQlLanguageNodeVariableReference,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeAbstractNode(
		node : GraphQlLanguageNodeAbstractNode,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDefinition(
		node : GraphQlLanguageNodeDefinition,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDescribedNode(
		node : GraphQlLanguageNodeDescribedNode,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDirectivesContainer(
		node : GraphQlLanguageNodeDirectivesContainer,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeNamedNode(
		node : GraphQlLanguageNodeNamedNode,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSelection(
		node : GraphQlLanguageNodeSelection,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSelectionSetContainer(
		node : GraphQlLanguageNodeSelectionSetContainer,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeType(
		node : GraphQlLanguageNodeType,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeValue(
		node : GraphQlLanguageNodeValue,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeComment(
		node : GraphQlLanguageNodeComment,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeDescription(
		node : GraphQlLanguageNodeDescription,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeNode(
		node : GraphQlLanguageNodeNode,
	): AppendableBlock? {
		return null
	}
	
	open fun renderGraphQlLanguageNodeSourceLocation(
		node : GraphQlLanguageNodeSourceLocation,
	): AppendableBlock? {
		return null
	}
	
	
	override fun processNode(
		traversalState : GraphQlLanguageNodeTraversalState,
	) {
		if (traversalState.context.phase != GraphQlLanguageNodeTraversalContextPhase.ENTER) {
			return
		}
		val nodeId = traversalState.context.nodeId
		val parentNodeId = traversalState.context.parentNodeId
		if (parentNodeId in touchedNodeIds) {
			touchedNodeIds.add(nodeId)
			return
		}
		
		val node = traversalState.node
		if (node is GraphQlLanguageNodeInterfaceTypeDefinitionProper) {
			val appendBlock = renderGraphQlLanguageNodeInterfaceTypeDefinitionProper(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeInterfaceTypeExtensionDefinition) {
			val appendBlock = renderGraphQlLanguageNodeInterfaceTypeExtensionDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeObjectTypeDefinitionProper) {
			val appendBlock = renderGraphQlLanguageNodeObjectTypeDefinitionProper(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeObjectTypeExtensionDefinition) {
			val appendBlock = renderGraphQlLanguageNodeObjectTypeExtensionDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeEnumTypeDefinitionProper) {
			val appendBlock = renderGraphQlLanguageNodeEnumTypeDefinitionProper(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeEnumTypeExtensionDefinition) {
			val appendBlock = renderGraphQlLanguageNodeEnumTypeExtensionDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeInputObjectTypeDefinitionProper) {
			val appendBlock = renderGraphQlLanguageNodeInputObjectTypeDefinitionProper(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeInputObjectTypeExtensionDefinition) {
			val appendBlock = renderGraphQlLanguageNodeInputObjectTypeExtensionDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeInterfaceTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeInterfaceTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeObjectTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeObjectTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeScalarTypeDefinitionProper) {
			val appendBlock = renderGraphQlLanguageNodeScalarTypeDefinitionProper(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeScalarTypeExtensionDefinition) {
			val appendBlock = renderGraphQlLanguageNodeScalarTypeExtensionDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeUnionTypeDefinitionProper) {
			val appendBlock = renderGraphQlLanguageNodeUnionTypeDefinitionProper(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeUnionTypeExtensionDefinition) {
			val appendBlock = renderGraphQlLanguageNodeUnionTypeExtensionDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeEnumTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeEnumTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeImplementingTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeImplementingTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeInputObjectTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeInputObjectTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeScalarTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeScalarTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeUnionTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeUnionTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDirectiveDefinition) {
			val appendBlock = renderGraphQlLanguageNodeDirectiveDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSchemaDefinitionProper) {
			val appendBlock = renderGraphQlLanguageNodeSchemaDefinitionProper(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSchemaExtensionDefinition) {
			val appendBlock = renderGraphQlLanguageNodeSchemaExtensionDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeBooleanValue) {
			val appendBlock = renderGraphQlLanguageNodeBooleanValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeEnumValueDefinition) {
			val appendBlock = renderGraphQlLanguageNodeEnumValueDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeFieldDefinition) {
			val appendBlock = renderGraphQlLanguageNodeFieldDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeFloatValue) {
			val appendBlock = renderGraphQlLanguageNodeFloatValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeInputValueDefinition) {
			val appendBlock = renderGraphQlLanguageNodeInputValueDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeIntValue) {
			val appendBlock = renderGraphQlLanguageNodeIntValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSDLNamedDefinition) {
			val appendBlock = renderGraphQlLanguageNodeSDLNamedDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSchemaDefinition) {
			val appendBlock = renderGraphQlLanguageNodeSchemaDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeStringValue) {
			val appendBlock = renderGraphQlLanguageNodeStringValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeAbstractDescribedNode) {
			val appendBlock = renderGraphQlLanguageNodeAbstractDescribedNode(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeArgument) {
			val appendBlock = renderGraphQlLanguageNodeArgument(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeArrayValue) {
			val appendBlock = renderGraphQlLanguageNodeArrayValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDirective) {
			val appendBlock = renderGraphQlLanguageNodeDirective(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDirectiveLocation) {
			val appendBlock = renderGraphQlLanguageNodeDirectiveLocation(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDocument) {
			val appendBlock = renderGraphQlLanguageNodeDocument(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeEnumValue) {
			val appendBlock = renderGraphQlLanguageNodeEnumValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeField) {
			val appendBlock = renderGraphQlLanguageNodeField(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeFragmentDefinition) {
			val appendBlock = renderGraphQlLanguageNodeFragmentDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeFragmentSpread) {
			val appendBlock = renderGraphQlLanguageNodeFragmentSpread(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeInlineFragment) {
			val appendBlock = renderGraphQlLanguageNodeInlineFragment(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeListType) {
			val appendBlock = renderGraphQlLanguageNodeListType(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeNonNullType) {
			val appendBlock = renderGraphQlLanguageNodeNonNullType(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeNullValue) {
			val appendBlock = renderGraphQlLanguageNodeNullValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeObjectField) {
			val appendBlock = renderGraphQlLanguageNodeObjectField(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeObjectValue) {
			val appendBlock = renderGraphQlLanguageNodeObjectValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeOperationDefinition) {
			val appendBlock = renderGraphQlLanguageNodeOperationDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeOperationTypeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeOperationTypeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSDLDefinition) {
			val appendBlock = renderGraphQlLanguageNodeSDLDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeScalarValue) {
			val appendBlock = renderGraphQlLanguageNodeScalarValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSelectionSet) {
			val appendBlock = renderGraphQlLanguageNodeSelectionSet(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeTypeName) {
			val appendBlock = renderGraphQlLanguageNodeTypeName(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeVariableDefinition) {
			val appendBlock = renderGraphQlLanguageNodeVariableDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeVariableReference) {
			val appendBlock = renderGraphQlLanguageNodeVariableReference(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeAbstractNode) {
			val appendBlock = renderGraphQlLanguageNodeAbstractNode(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDefinition) {
			val appendBlock = renderGraphQlLanguageNodeDefinition(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDescribedNode) {
			val appendBlock = renderGraphQlLanguageNodeDescribedNode(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDirectivesContainer) {
			val appendBlock = renderGraphQlLanguageNodeDirectivesContainer(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeNamedNode) {
			val appendBlock = renderGraphQlLanguageNodeNamedNode(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSelection) {
			val appendBlock = renderGraphQlLanguageNodeSelection(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSelectionSetContainer) {
			val appendBlock = renderGraphQlLanguageNodeSelectionSetContainer(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeType) {
			val appendBlock = renderGraphQlLanguageNodeType(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeValue) {
			val appendBlock = renderGraphQlLanguageNodeValue(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeComment) {
			val appendBlock = renderGraphQlLanguageNodeComment(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeDescription) {
			val appendBlock = renderGraphQlLanguageNodeDescription(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeNode) {
			val appendBlock = renderGraphQlLanguageNodeNode(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		if (node is GraphQlLanguageNodeSourceLocation) {
			val appendBlock = renderGraphQlLanguageNodeSourceLocation(node)
			if (appendBlock != null) {
				touchedNodeIds.add(nodeId)
				appendBlock(writer)
				return
			}
		}
		
		
	}
}
