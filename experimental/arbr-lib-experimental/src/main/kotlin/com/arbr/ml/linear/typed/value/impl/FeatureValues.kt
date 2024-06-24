package com.arbr.ml.linear.typed.value.impl

import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.base.RowVector
import com.arbr.platform.ml.linear.typed.value.base.RowVectorTypedValue

/**
 * Simple, concrete value types backed by feature row-vectors of bound dimension F.
 * Unanswered question: should values be nullable with the zero vector by default? Will need to define embeddings into
 * structural supertypes.
 */
object F {

    abstract class Value<V>(
        override val value: V,
        override val tensor: RowVector<Dim.VariableF>,
    ) : RowVectorTypedValue<V, Dim.VariableF>

    /**
     * A string backed by a feature vector
     */
    data class String(
        override val value: kotlin.String,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.String>(value, tensor)

    data class StringNullable(
        override val value: kotlin.String?,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.String?>(value, tensor)

    data class Boolean(
        override val value: kotlin.Boolean,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.Boolean>(value, tensor)

    data class BooleanNullable(
        override val value: kotlin.Boolean?,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.Boolean?>(value, tensor)

    data class Int(
        override val value: kotlin.Int,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.Int>(value, tensor)

    data class IntNullable(
        override val value: kotlin.Int?,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.Int?>(value, tensor)

    data class Long(
        override val value: kotlin.Long,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.Long>(value, tensor)

    data class LongNullable(
        override val value: kotlin.Long?,
        override val tensor: RowVector<Dim.VariableF>,
    ) : Value<kotlin.Long?>(value, tensor)

}
