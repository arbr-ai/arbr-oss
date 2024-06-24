package com.arbr.platform.ml.linear.typed.shape

import com.arbr.platform.ml.linear.typed.shape.Shape

/**
 * A basic dimension.
 * A dimension is also a singleton Shape.
 */
sealed interface Dim: Shape {
    interface AtMost<N: Dim>: Dim
    interface AtLeast<N: Dim>: Dim

    object Zero: Dim, AtMost<Zero>, AtLeast<Zero>
    object One: Dim, AtMost<One>, AtLeast<One>
    object Two: Dim, AtMost<Two>, AtLeast<Two>

    object VariableA: Dim, AtMost<VariableA>, AtLeast<VariableA>
    object VariableB: Dim, AtMost<VariableB>, AtLeast<VariableB>
    object VariableC: Dim, AtMost<VariableC>, AtLeast<VariableC>
    object VariableD: Dim, AtMost<VariableD>, AtLeast<VariableD>
    object VariableE: Dim, AtMost<VariableE>, AtLeast<VariableE>
    object VariableF: Dim, AtMost<VariableF>, AtLeast<VariableF>
    object VariableG: Dim, AtMost<VariableG>, AtLeast<VariableG>
    object VariableH: Dim, AtMost<VariableH>, AtLeast<VariableH>
    object VariableM: Dim, AtMost<VariableM>, AtLeast<VariableM>
    object VariableN: Dim, AtMost<VariableN>, AtLeast<VariableN>
    object VariableP: Dim, AtMost<VariableP>, AtLeast<VariableP>
    object VariableQ: Dim, AtMost<VariableQ>, AtLeast<VariableQ>
    object VariableR: Dim, AtMost<VariableR>, AtLeast<VariableR>
    object VariableS: Dim, AtMost<VariableS>, AtLeast<VariableS>
    object VariableT: Dim, AtMost<VariableT>, AtLeast<VariableT>
    object VariableU: Dim, AtMost<VariableU>, AtLeast<VariableU>
    object VariableV: Dim, AtMost<VariableV>, AtLeast<VariableV>
    object VariableW: Dim, AtMost<VariableW>, AtLeast<VariableW>
    object VariableX: Dim, AtMost<VariableX>, AtLeast<VariableX>
    object VariableY: Dim, AtMost<VariableY>, AtLeast<VariableY>
    object VariableZ: Dim, AtMost<VariableZ>, AtLeast<VariableZ>

    interface NonZero<I: Dim>: Dim

    /**
     * A dimension which is the literal sum of the two parameter dimensions, not to be confused with a Sum Type in the
     * sense of a Sigma Type
     */
    interface SumOf<M: Dim, N: Dim>: Dim

    /**
     * A dimension which is the literal product of the two parameter dimensions, not to be confused with a Product Type
     * in the sense of a Pi Type
     */
    interface ProductOf<M: Dim, N: Dim>: Dim

    fun <M: Dim, N: NonZero<NI>, NI: Dim> SumOf<M, N>.nonZero(): NonZero<SumOf<M, NI>> {
        return object : NonZero<SumOf<M, NI>> {}
    }
}

