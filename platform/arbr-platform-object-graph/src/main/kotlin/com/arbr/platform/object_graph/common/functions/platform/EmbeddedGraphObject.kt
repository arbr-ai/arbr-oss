package com.arbr.og.object_model.common.functions.platform

import com.arbr.og.object_model.common.functions.spec.base.FunctionInputElement
import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.shape.Dim

abstract class EmbeddedGraphObject<U: FunctionInputElement, DimF: Dim, Vec: ColumnVector<DimF>>(
    val graphObject: U,

    /**
     * TODO: Transition to valued tensors
     */
    val embeddedForm: Vec,
): DistanceComparable<EmbeddedGraphObject<U, DimF, Vec>> {

    override fun distance(other: EmbeddedGraphObject<U, DimF, Vec>): Double {
        // TODO: Simplify
        return embeddedForm.plus(other.embeddedForm.scale(-1.0)).normF()
    }
}