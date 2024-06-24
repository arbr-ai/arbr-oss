package com.arbr.ml.dl.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

interface DlTrainingSpec<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K>
