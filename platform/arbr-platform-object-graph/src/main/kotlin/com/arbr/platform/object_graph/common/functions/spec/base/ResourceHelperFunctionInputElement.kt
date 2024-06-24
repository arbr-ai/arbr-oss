package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.og.object_model.common.properties.FieldValueViewContainer
import com.arbr.og.object_model.common.values.SourcedValue

sealed interface FunctionInputElement

data class FunctionInputSingleton<S: SourcedValue<*>>(
    val value: S,
) : FunctionInputElement

data class FunctionInputObjectList<E : FunctionInputElement>(
    val elements: FieldValueViewContainer<E, E, *>,
) : FunctionInputElement

data class FunctionInputStruct1<S1 : FunctionInputElement>(
    val t1: S1,
) : FunctionInputElement

data class FunctionInputStruct2<S1 : FunctionInputElement, S2 : FunctionInputElement>(
    val t1: S1,
    val t2: S2,
) : FunctionInputElement

data class FunctionInputStruct3<S1 : FunctionInputElement, S2 : FunctionInputElement, S3 : FunctionInputElement>(
    val t1: S1,
    val t2: S2,
    val t3: S3,
) : FunctionInputElement

data class FunctionInputStruct4<S1 : FunctionInputElement, S2 : FunctionInputElement, S3 : FunctionInputElement, S4 : FunctionInputElement>(
    val t1: S1,
    val t2: S2,
    val t3: S3,
    val t4: S4,
) : FunctionInputElement

data class FunctionInputStruct5<S1 : FunctionInputElement, S2 : FunctionInputElement, S3 : FunctionInputElement, S4 : FunctionInputElement, S5 : FunctionInputElement>(
    val t1: S1,
    val t2: S2,
    val t3: S3,
    val t4: S4,
    val t5: S5,
) : FunctionInputElement