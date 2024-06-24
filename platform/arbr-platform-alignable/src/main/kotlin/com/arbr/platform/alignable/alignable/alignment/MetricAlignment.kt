package com.arbr.platform.alignable.alignable.alignment

import com.arbr.platform.data_structures_common.partial_order.LinearOrderList
import com.arbr.platform.ml.optimization.base.ParameterValue
import com.arbr.platform.ml.optimization.base.ParameterValueProvider

// import com.arbr.platform.ml.optimization.base.ParameterValueProvider

sealed class MetricAlignment<E, AlignmentOperation>(
    open val parameterValueProvider: ParameterValueProvider,

    open val operations: List<AlignmentOperation>,

    // Cost of edit
    open val costMetric: ParameterValue,

    // The source element which was the input to the operation.
    override val sourceElement: E,

    // The target element which resulted from the operation.
    override val targetElement: E,
): OrderAlignment<E, AlignmentOperation, LinearOrderList<AlignmentOperation>> {

    override val operationOrder: LinearOrderList<AlignmentOperation>?
        get() = LinearOrderList(operations)

    override val cost: Double get() = costMetric.value

    data class Align<E, AlignmentOperation>(
        override val parameterValueProvider: ParameterValueProvider,

        override val operations: List<AlignmentOperation>,

        // Cost of edit
        override val costMetric: ParameterValue,

        // The source element which was the input to the operation.
        override val sourceElement: E,

        // The target element which resulted from the operation.
        override val targetElement: E,
    ): MetricAlignment<E, AlignmentOperation>(parameterValueProvider, operations, costMetric, sourceElement, targetElement)

    data class Equal<E, AlignmentOperation>(
        override val parameterValueProvider: ParameterValueProvider,

        // The source element which was the input to the operation.
        override val sourceElement: E,

        // The target element which resulted from the operation.
        override val targetElement: E,
    ): MetricAlignment<E, AlignmentOperation>(parameterValueProvider, emptyList(), ParameterValue(0.0), sourceElement, targetElement)

    companion object {

        fun <E, AlignmentOperation> of(
            parameterValueProvider: ParameterValueProvider,
            operations: List<AlignmentOperation>,
            cost: ParameterValue,
            sourceElement: E,
            targetElement: E,
        ): MetricAlignment<E, AlignmentOperation> {
            return if (operations.isEmpty()) {
                Equal(parameterValueProvider, sourceElement, targetElement)
            } else {
                Align(parameterValueProvider, operations, cost, sourceElement, targetElement)
            }
        }

    }
}
