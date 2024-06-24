package com.arbr.ml.dl.base

import com.arbr.platform.ml.linear.typed.base.StructTensor
import com.arbr.platform.ml.linear.typed.base.*
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

interface DlDataset<ST: Shape, SU: Shape, F: GroundField<K>, K, Size: Dim>:
    StructTensor.Two<
            Tensor<Shape.Product<Size, ST>, F, K>,
            Shape.Product<Size, ST>,
            Tensor<Shape.Product<Size, SU>, F, K>,
            Shape.Product<Size, SU>,
            F, K>