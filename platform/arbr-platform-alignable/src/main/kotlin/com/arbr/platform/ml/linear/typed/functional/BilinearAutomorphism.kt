package com.arbr.platform.ml.linear.typed.functional

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

/**
 * Bilinear map along the same type, i.e. T -> T
 * A Positive Semi-Definite Bilinear Automorphism is an inner product
 *
 * A Bilinear Automorphism defines a natural norm `applyBilinear(t, t)`, but this is only valid as such if the map is
 * an inner product.
 */
interface BilinearAutomorphism<T: Tensor<ST, F, K>, ST: Shape, F: GroundField<K>, K>: BilinearMap<T, ST, T, ST, F, K>