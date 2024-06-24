package com.arbr.graphql.lang.common

///**
// * Sum type for the possible subclasses of [graphql.language.Value]:
// *  - [graphql.language.ArrayValue]
// *  - [graphql.language.EnumValue]
// *  - [graphql.language.NullValue]
// *  - [graphql.language.ObjectValue]
// *  - [graphql.language.VariableReference]
// *  - [graphql.language.ScalarValue]:
// *  -   [graphql.language.BooleanValue]
// *  -   [graphql.language.FloatValue]
// *  -   [graphql.language.IntValue]
// *  -   [graphql.language.StringValue]
// */
//sealed interface GraphQlLanguageNodeValue {
//    data class ArrayValue(val innerValue: GraphQlLanguageNodeArrayValue): GraphQlLanguageNodeValue
//    data class EnumValue(val innerValue: GraphQlLanguageNodeEnumValue): GraphQlLanguageNodeValue
//    data class NullValue(val innerValue: GraphQlLanguageNodeNullValue): GraphQlLanguageNodeValue
//    data class ObjectValue(val innerValue: GraphQlLanguageNodeObjectValue): GraphQlLanguageNodeValue
//
//    sealed interface ScalarValue: GraphQlLanguageNodeValue {
//        data class BooleanValue(val innerValue: GraphQlLanguageNodeBooleanValue): ScalarValue
//        data class FloatValue(val innerValue: GraphQlLanguageNodeFloatValue): ScalarValue
//        data class IntValue(val innerValue: GraphQlLanguageNodeIntValue): ScalarValue
//        data class StringValue(val innerValue: GraphQlLanguageNodeStringValue): ScalarValue
//    }
//}
