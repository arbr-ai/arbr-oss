package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.og.object_model.common.properties.FieldValueViewContainer
import com.arbr.og.object_model.common.values.SourcedValue

interface ResourceHelperFunctionInputSpecContext {
    fun <S : SourcedValue<*>> element(input: S): FunctionInputSingleton<S>
    fun <E : FunctionInputElement> elementList(inputs: FieldValueViewContainer<E, E, *>): FunctionInputObjectList<E>

    fun <E1 : FunctionInputElement> struct(element1: E1): FunctionInputStruct1<E1>
    fun <
            E1 : FunctionInputElement,
            E2 : FunctionInputElement,
            > struct(
        element1: E1,
        element2: E2,
    ): FunctionInputStruct2<E1, E2>
}