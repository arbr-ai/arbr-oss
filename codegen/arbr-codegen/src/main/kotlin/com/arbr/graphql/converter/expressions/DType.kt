package com.arbr.graphql.converter.expressions

/**
 * Cross-language system of domain types being represented, e.g. "Dog", "Cat" such that a DType and an Expr together
 * carry enough information for unambiguous conversion across expressions. For example we might want to construct a
 * transform (Node<DType.Gql.Field, GqlLang.LiteralValue> -> Node<DType.Gql.Field, KtLang.LiteralValue>) in a general sense, then
 * represent the same transform as (Node<DType.Dog, GqlLang.LiteralValue -> KtLang.LiteralValue>)
 *
 * For simplicity we can enumerate the varieties of DTypes used here for Gql and Kt schema conversion.
 */
sealed interface DType {

    /**
     * Representations of Gql Schema in the general sense as domain types
     */
    sealed interface GqlSchema: DType {

        sealed interface GqlType: GqlSchema {
            data object AbstractDescribedNode : GqlType
            data object AbstractNode : GqlType
            data object Argument : GqlType
            data object ArrayValue : GqlType
            data object BooleanValue : GqlType
            data object Comment : GqlType
            data object Definition : GqlType
            data object DescribedNode : GqlType
            data object Description : GqlType
            data object Directive : GqlType
            data object DirectiveDefinition : GqlType
            data object DirectiveLocation : GqlType
            data object DirectivesContainer : GqlType
            data object Document : GqlType
            data object EnumTypeDefinition : GqlType
            data object EnumTypeExtensionDefinition : GqlType
            data object EnumValue : GqlType
            data object EnumValueDefinition : GqlType
            data object Field : GqlType
            data object FieldDefinition : GqlType
            data object FloatValue : GqlType
            data object FragmentDefinition : GqlType
            data object FragmentSpread : GqlType
            data object ImplementingTypeDefinition : GqlType
            data object InlineFragment : GqlType
            data object InputObjectTypeDefinition : GqlType
            data object InputObjectTypeExtensionDefinition : GqlType
            data object InputValueDefinition : GqlType
            data object IntValue : GqlType
            data object InterfaceTypeDefinition : GqlType
            data object InterfaceTypeExtensionDefinition : GqlType
            data object ListType : GqlType
            data object NamedNode : GqlType
            data object Node : GqlType
            data object NonNullType : GqlType
            data object NullValue : GqlType
            data object ObjectField : GqlType
            data object ObjectTypeDefinition : GqlType
            data object ObjectTypeExtensionDefinition : GqlType
            data object ObjectValue : GqlType
            data object OperationDefinition : GqlType
            data object OperationTypeDefinition : GqlType
            data object SDLDefinition : GqlType
            data object SDLNamedDefinition : GqlType
            data object ScalarTypeDefinition : GqlType
            data object ScalarTypeExtensionDefinition : GqlType
            data object ScalarValue : GqlType
            data object SchemaDefinition : GqlType
            data object SchemaExtensionDefinition : GqlType
            data object Selection : GqlType
            data object SelectionSet : GqlType
            data object SelectionSetContainer : GqlType
            data object SourceLocation : GqlType
            data object StringValue : GqlType
            data object Type : GqlType
            data object TypeDefinition : GqlType
            data object TypeName : GqlType
            data object UnionTypeDefinition : GqlType
            data object UnionTypeExtensionDefinition : GqlType
            data object Value : GqlType
            data object VariableDefinition : GqlType
            data object VariableReference : GqlType
        }

    }

//    sealed interface KtLang {
//
//        sealed interface KtClass {
//            data object AbstractDescribedNode : KtClass
//            data object AbstractNode : KtClass
//            data object Argument : KtClass
//            data object ArrayValue : KtClass
//            data object BooleanValue : KtClass
//            data object Comment : KtClass
//            data object Definition : KtClass
//            data object DescribedNode : KtClass
//            data object Description : KtClass
//            data object Directive : KtClass
//            data object DirectiveDefinition : KtClass
//            data object DirectiveLocation : KtClass
//            data object DirectivesContainer : KtClass
//            data object Document : KtClass
//            data object EnumTypeDefinition : KtClass
//            data object EnumTypeExtensionDefinition : KtClass
//            data object EnumValue : KtClass
//            data object EnumValueDefinition : KtClass
//            data object Field : KtClass
//            data object FieldDefinition : KtClass
//            data object FloatValue : KtClass
//            data object FragmentDefinition : KtClass
//            data object FragmentSpread : KtClass
//            data object ImplementingTypeDefinition : KtClass
//            data object InlineFragment : KtClass
//            data object InputObjectTypeDefinition : KtClass
//            data object InputObjectTypeExtensionDefinition : KtClass
//            data object InputValueDefinition : KtClass
//            data object IntValue : KtClass
//            data object InterfaceTypeDefinition : KtClass
//            data object InterfaceTypeExtensionDefinition : KtClass
//            data object ListType : KtClass
//            data object NamedNode : KtClass
//            data object Node : KtClass
//            data object NonNullType : KtClass
//            data object NullValue : KtClass
//            data object ObjectField : KtClass
//            data object ObjectTypeDefinition : KtClass
//            data object ObjectTypeExtensionDefinition : KtClass
//            data object ObjectValue : KtClass
//            data object OperationDefinition : KtClass
//            data object OperationTypeDefinition : KtClass
//            data object SDLDefinition : KtClass
//            data object SDLNamedDefinition : KtClass
//            data object ScalarTypeDefinition : KtClass
//            data object ScalarTypeExtensionDefinition : KtClass
//            data object ScalarValue : KtClass
//            data object SchemaDefinition : KtClass
//            data object SchemaExtensionDefinition : KtClass
//            data object Selection : KtClass
//            data object SelectionSet : KtClass
//            data object SelectionSetContainer : KtClass
//            data object SourceLocation : KtClass
//            data object StringValue : KtClass
//            data object Type : KtClass
//            data object TypeDefinition : KtClass
//            data object TypeName : KtClass
//            data object UnionTypeDefinition : KtClass
//            data object UnionTypeExtensionDefinition : KtClass
//            data object Value : KtClass
//            data object VariableDefinition : KtClass
//            data object VariableReference : KtClass
//        }
//
//    }

}