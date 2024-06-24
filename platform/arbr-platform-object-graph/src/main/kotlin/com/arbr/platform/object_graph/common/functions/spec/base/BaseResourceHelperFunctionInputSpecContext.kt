package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.og.object_model.common.properties.FieldValueViewContainer
import com.arbr.og.object_model.common.values.SourcedValue

class BaseResourceHelperFunctionInputSpecContext : ResourceHelperFunctionInputSpecContext {

//    override fun <V> SourcedValue<V>.serializeWith(serializer: InputPropertySerializer<V>) {
////        val serializedValue = serializer.serialize(this.value)
////        elements.add(ResourceHelperFunctionInputElement.Singleton(SourcedValue(serializedValue, this.source)))
//    }

    /**
     * These are essentially convenience constructors
     */

    override fun <S : SourcedValue<*>> element(input: S): FunctionInputSingleton<S> {
        return FunctionInputSingleton(input)
    }

    override fun <E : FunctionInputElement> elementList(inputs: FieldValueViewContainer<E, E, *>): FunctionInputObjectList<E> {
        return FunctionInputObjectList(inputs)
    }

    override fun <E1 : FunctionInputElement> struct(element1: E1): FunctionInputStruct1<E1> {
        return FunctionInputStruct1(element1)
    }

    override fun <E1 : FunctionInputElement, E2 : FunctionInputElement> struct(
        element1: E1,
        element2: E2
    ): FunctionInputStruct2<E1, E2> {
        return FunctionInputStruct2(element1, element2)
    }
}