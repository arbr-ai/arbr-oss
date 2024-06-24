package com.arbr.platform.object_graph.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

object ObjectTypeTensorFactory {

    @Suppress("UNCHECKED_CAST")
    fun <OT: ObjectModel.ObjectType<V, ST, SF, SVT>, V, ST: Dim, SF: Shape, SVT: ObjectModel.ObjectValue<V, ST, SF, SVT>> typeTensor(
        typeClass: Class<OT>,
        shape: ST,
    ): Tensor<ST, GroundField.Real, Scalar> {
        return SingletonTensorImpl<Dim, GroundField.Real, Scalar>(
            typeClass.simpleName, // TODO: Inject via template
            shape,
        ) as Tensor<ST, GroundField.Real, Scalar>
    }

}
