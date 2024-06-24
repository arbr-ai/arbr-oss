package com.arbr.graphql.lang.common

import com.arbr.graphql.lang.node.GraphQlLanguageNodeTraversalContext
import com.arbr.graphql.lang.node.GraphQlLanguageNodeTraversalContextPhase
import com.arbr.graphql.lang.node.GraphQlLanguageNodeTraversalState
import com.arbr.graphql.lang.node.TraversalContextNodeIdContainer
import graphql.language.Node
import graphql.util.TraverserContext
import java.util.*

class CompilerProcessNodeVisitor(
	private val nodeProcessor: GraphQlNodeProcessor,
): graphql.language.NodeVisitor {
	private val transformer = GraphQlLanguageNodeTransformer()

    private fun visitNodeAndGetContext(
        nodeType: GraphQlSimpleNodeType,
        context: TraverserContext<Node<Node<*>>>,
    ): GraphQlLanguageNodeTraversalContext {
        val keyClass = TraversalContextNodeIdContainer::class.java
        val nodeParentId = context.getVarFromParents(keyClass) ?: null
        val currentId = context.getVar(keyClass) ?: null
        val nodeId = if (currentId == null) {
            val newId = UUID.randomUUID().toString()
            context.setVar(keyClass, TraversalContextNodeIdContainer(newId))
            newId
        } else {
            currentId.nodeId
        }

		val phase = when (context.phase) {
			TraverserContext.Phase.LEAVE -> GraphQlLanguageNodeTraversalContextPhase.LEAVE
			TraverserContext.Phase.ENTER -> GraphQlLanguageNodeTraversalContextPhase.ENTER
			TraverserContext.Phase.BACKREF -> GraphQlLanguageNodeTraversalContextPhase.BACKREF
			null -> throw IllegalArgumentException("Null traversal phase during node visit")
		}

        return GraphQlLanguageNodeTraversalContext(
            nodeType,
            nodeId,
            phase,
            nodeParentId?.nodeId,
        )
    }
	override fun visitScalarTypeDefinition(
		node : graphql.language.ScalarTypeDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertScalarTypeDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_SCALAR_TYPE_DEFINITION_PROPER
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitArrayValue(
		node : graphql.language.ArrayValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertArrayValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_ARRAY_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitBooleanValue(
		node : graphql.language.BooleanValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertBooleanValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_BOOLEAN_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitDirectiveDefinition(
		node : graphql.language.DirectiveDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertDirectiveDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_DIRECTIVE_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitDirectiveLocation(
		node : graphql.language.DirectiveLocation,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertDirectiveLocation(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_DIRECTIVE_LOCATION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitDocument(
		node : graphql.language.Document,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertDocument(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_DOCUMENT
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitEnumTypeDefinition(
		node : graphql.language.EnumTypeDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertEnumTypeDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_ENUM_TYPE_DEFINITION_PROPER
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitEnumValue(
		node : graphql.language.EnumValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertEnumValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_ENUM_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitEnumValueDefinition(
		node : graphql.language.EnumValueDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertEnumValueDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_ENUM_VALUE_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitFieldDefinition(
		node : graphql.language.FieldDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertFieldDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_FIELD_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitFloatValue(
		node : graphql.language.FloatValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertFloatValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_FLOAT_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitInputObjectTypeDefinition(
		node : graphql.language.InputObjectTypeDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertInputObjectTypeDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_INPUT_OBJECT_TYPE_DEFINITION_PROPER
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitInputValueDefinition(
		node : graphql.language.InputValueDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertInputValueDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_INPUT_VALUE_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitIntValue(
		node : graphql.language.IntValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertIntValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_INT_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitInterfaceTypeDefinition(
		node : graphql.language.InterfaceTypeDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertInterfaceTypeDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_INTERFACE_TYPE_DEFINITION_PROPER
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitListType(
		node : graphql.language.ListType,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertListType(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_LIST_TYPE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitNonNullType(
		node : graphql.language.NonNullType,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertNonNullType(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_NON_NULL_TYPE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitNullValue(
		node : graphql.language.NullValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertNullValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_NULL_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitObjectTypeDefinition(
		node : graphql.language.ObjectTypeDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertObjectTypeDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_OBJECT_TYPE_DEFINITION_PROPER
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitObjectValue(
		node : graphql.language.ObjectValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertObjectValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_OBJECT_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitOperationDefinition(
		node : graphql.language.OperationDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertOperationDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_OPERATION_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitOperationTypeDefinition(
		node : graphql.language.OperationTypeDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertOperationTypeDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_OPERATION_TYPE_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitSchemaDefinition(
		node : graphql.language.SchemaDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertSchemaDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_SCHEMA_DEFINITION_PROPER
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitSelectionSet(
		node : graphql.language.SelectionSet,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertSelectionSet(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_SELECTION_SET
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitStringValue(
		node : graphql.language.StringValue,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertStringValue(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_STRING_VALUE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitTypeName(
		node : graphql.language.TypeName,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertTypeName(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_TYPE_NAME
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitUnionTypeDefinition(
		node : graphql.language.UnionTypeDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertUnionTypeDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_UNION_TYPE_DEFINITION_PROPER
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitVariableReference(
		node : graphql.language.VariableReference,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertVariableReference(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_VARIABLE_REFERENCE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitInlineFragment(
		node : graphql.language.InlineFragment,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertInlineFragment(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_INLINE_FRAGMENT
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitFragmentSpread(
		node : graphql.language.FragmentSpread,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertFragmentSpread(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_FRAGMENT_SPREAD
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitFragmentDefinition(
		node : graphql.language.FragmentDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertFragmentDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_FRAGMENT_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitArgument(
		node : graphql.language.Argument,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertArgument(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_ARGUMENT
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitDirective(
		node : graphql.language.Directive,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertDirective(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_DIRECTIVE
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitObjectField(
		node : graphql.language.ObjectField,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertObjectField(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_OBJECT_FIELD
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitVariableDefinition(
		node : graphql.language.VariableDefinition,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertVariableDefinition(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_VARIABLE_DEFINITION
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
	override fun visitField(
		node : graphql.language.Field,
		data : graphql.util.TraverserContext<graphql.language.Node<graphql.language.Node<*>>>,
	): graphql.util.TraversalControl {
		val converted = transformer.convertField(node)
		val nodeType = GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_FIELD
		val context = visitNodeAndGetContext(nodeType, data)
		nodeProcessor.processNode(GraphQlLanguageNodeTraversalState(converted, context))
		
		return graphql.util.TraversalControl.CONTINUE
	}
	
}
