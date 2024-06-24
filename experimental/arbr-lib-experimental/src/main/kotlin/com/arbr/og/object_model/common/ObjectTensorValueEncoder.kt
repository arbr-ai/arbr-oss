package com.arbr.og.object_model.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.RowVector
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.impl.TypedRowVector
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

fun interface ObjectTensorValueEncoder<ValueType> {

    fun encode(value: ValueType): Tensor<Dim.VariableF, GroundField.Real, Scalar>

    companion object {

        private val vectorSize = 4 // Placeholder

        private fun squeeze(
            typeIdentifier: String,
            rowVector: RowVector<Dim.VariableF>
        ): Tensor<Dim.VariableF, GroundField.Real, Scalar> = SingletonTensorImpl(
            typeIdentifier,
            rowVector.numColsShape
        ) // TODO: Allow for valued tensors

        private val zero = squeeze("Null", TypedRowVector(DoubleArray(vectorSize) { 0.0 }, Dim.VariableF))

        val StringEncoder = ObjectTensorValueEncoder<String> { _ ->
            squeeze("String", TypedRowVector(DoubleArray(vectorSize) { 0.0 }, Dim.VariableF))
        }

        val IntEncoder = ObjectTensorValueEncoder<Int> { _ ->
            squeeze("Int", TypedRowVector(DoubleArray(vectorSize) { 0.0 }, Dim.VariableF))
        }

        val LongEncoder = ObjectTensorValueEncoder<Long> { _ ->
            squeeze("Long", TypedRowVector(DoubleArray(vectorSize) { 0.0 }, Dim.VariableF))
        }

        val BooleanEncoder = ObjectTensorValueEncoder<Boolean> { _ ->
            squeeze("Boolean", TypedRowVector(DoubleArray(vectorSize) { 0.0 }, Dim.VariableF))
        }

        val DoubleEncoder = ObjectTensorValueEncoder<Double> { _ ->
            squeeze("Double", TypedRowVector(DoubleArray(vectorSize) { 0.0 }, Dim.VariableF))
        }

        fun <T : Any> encode(value: T?): Tensor<Dim.VariableF, GroundField.Real, Scalar> {
            return when (value) {
                null -> zero
                is String -> StringEncoder.encode(value)
                is Boolean -> BooleanEncoder.encode(value)
                is Int -> IntEncoder.encode(value)
                is Long -> LongEncoder.encode(value)
                is Double -> DoubleEncoder.encode(value)
                else -> throw UnsupportedOperationException(value::class.java.name)
            }
        }

        fun <ValueType, ST : Shape, SF : Shape, SourcedValueType : ObjectModel.ObjectValue<ValueType, ST, SF, SourcedValueType>> encodeByType(
            objectType: ObjectModel.ObjectType<ValueType, ST, SF, SourcedValueType>,
            value: ValueType,
        ): Tensor<SF, GroundField.Real, Scalar> {
            // TODO: implement
            @Suppress("UNCHECKED_CAST")
            return encode("") as Tensor<SF, GroundField.Real, Scalar>
        }

    }
}
