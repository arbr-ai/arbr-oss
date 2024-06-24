package com.arbr.ml.dl.base

import com.arbr.platform.ml.linear.typed.base.*
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import reactor.core.publisher.Mono

/**
 * A provider of Deep Learning functionality via DJL
 */
interface DlProvider {

    fun<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K> initialize(
        modelSpec: DlModel.Spec<T, ST, U, SU, F, K>,
    ): Mono<DlModel.Untrained<T, ST, U, SU, F, K>>

    fun<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K> trainEpoch(
        modelSpec: DlModel.Untrained<T, ST, U, SU, F, K>,
    ): Mono<DlModel.PartiallyTrained<T, ST, U, SU, F, K>>

    fun<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K> train(
        modelSpec: DlModel.Untrained<T, ST, U, SU, F, K>,
    ): Mono<DlModel.Trained<T, ST, U, SU, F, K>>

}
