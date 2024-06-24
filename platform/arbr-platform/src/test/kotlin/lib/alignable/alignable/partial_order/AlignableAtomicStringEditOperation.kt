package com.arbr.alignable.alignable.partial_order
import com.arbr.platform.alignable.alignable.AtomicAlignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.alignable.alignable.edit_operation.AlignableEditOperation
import com.arbr.ml.optimization.base.ParameterValue
import com.arbr.ml.optimization.base.ParameterValueProvider

@Suppress("DataClassPrivateConstructor")
data class AlignableAtomicStringEditOperation private constructor(
    val parameterValueProvider: ParameterValueProvider,
    val s: String,
    private val atomic: AtomicAlignable<String>,
) : AlignableEditOperation<String, AlignableAtomicStringEditOperation, String> {

    constructor(
        parameterValueProvider: ParameterValueProvider,
        s: String): this(parameterValueProvider, s, AtomicAlignable(s))

    override fun align(e: AlignableAtomicStringEditOperation): MetricAlignment<AlignableAtomicStringEditOperation, String> {
        return when (val innerAlignment = atomic.align(e.atomic)) {
            is Alignment.Align -> MetricAlignment.Align(
                parameterValueProvider,
                innerAlignment.operations,
                ParameterValue(innerAlignment.cost),
                this,
                e,
            )

            is Alignment.Equal -> MetricAlignment.Equal(
                parameterValueProvider,
                this,
                e,
            )
        }
    }

    override fun empty(): AlignableAtomicStringEditOperation {
        return AlignableAtomicStringEditOperation(parameterValueProvider, s, atomic.empty())
    }

    override fun applyTo(state: String): String {
        return s
    }

    override fun applyAlignment(alignmentOperations: List<String>): AlignableAtomicStringEditOperation {
        return if (alignmentOperations.isEmpty()) {
            this
        } else {
            AlignableAtomicStringEditOperation(parameterValueProvider, alignmentOperations.last())
        }
    }
}