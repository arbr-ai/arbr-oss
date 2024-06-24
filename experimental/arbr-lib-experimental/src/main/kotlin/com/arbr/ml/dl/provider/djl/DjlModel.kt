package com.arbr.ml.dl.provider.djl

import com.arbr.ml.dl.base.DlModel
import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.ml.linear.typed.functional.TensorFunction

/**
 * A Deep Learning model in abstract, representing the lifecycle from Spec -> Untrained -> PartiallyTrained -> Trained
 */
sealed interface DjlModel<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K> {

    /**
     * Model specification with signature S -> T
     */
    data class Spec<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K>(
        val name: String,
        val tensorFunction: TensorFunction<T, ST, U, SU, F, K>,

        ):
        DjlModel<T, ST, U, SU, F, K>, DlModel.Spec<T, ST, U, SU, F, K>

    /**
     * A model constructed but not yet trained.
     */
    interface Untrained<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K>:
        DjlModel<T, ST, U, SU, F, K>

    /**
     * A partially (or completely) trained model.
     */
    interface PartiallyTrained<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K>:
        DjlModel<T, ST, U, SU, F, K>

    /**
     * A completely trained model.
     */
    interface Trained<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K>:
        PartiallyTrained<T, ST, U, SU, F, K>

}