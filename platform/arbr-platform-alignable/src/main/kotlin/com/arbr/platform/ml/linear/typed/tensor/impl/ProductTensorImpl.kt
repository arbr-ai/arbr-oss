package com.arbr.platform.ml.linear.typed.tensor.impl

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.ProductTensor
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

class ProductTensorImpl<S : Shape.Product<S0, S1>, S0 : Shape, S1 : Shape, F : GroundField<K>, K>(
    override val tensor0: Tensor<S0, F, K>,
    override val tensor1: Tensor<S1, F, K>,
    override val shape: S
) : ProductTensor<S, S0, S1, F, K>
