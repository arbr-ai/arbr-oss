package com.arbr.ml.dl.provider.djl

import com.arbr.ml.dl.base.DlModel
import com.arbr.ml.dl.base.DlProvider
import com.arbr.platform.ml.linear.typed.base.*
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import reactor.core.publisher.Mono

/**
 * Provider of Deep Learning functionality via DJL
 */
class DjlProvider : DlProvider {
    override fun <T : Tensor<ST, F, K>, ST : Shape, U : Tensor<SU, F, K>, SU : Shape, F : GroundField<K>, K> initialize(
        modelSpec: DlModel.Spec<T, ST, U, SU, F, K>
    ): Mono<DlModel.Untrained<T, ST, U, SU, F, K>> {
        TODO("Not yet implemented")
    }

    override fun <T : Tensor<ST, F, K>, ST : Shape, U : Tensor<SU, F, K>, SU : Shape, F : GroundField<K>, K> trainEpoch(
        modelSpec: DlModel.Untrained<T, ST, U, SU, F, K>
    ): Mono<DlModel.PartiallyTrained<T, ST, U, SU, F, K>> {
        TODO("Not yet implemented")
    }

    override fun <T : Tensor<ST, F, K>, ST : Shape, U : Tensor<SU, F, K>, SU : Shape, F : GroundField<K>, K> train(
        modelSpec: DlModel.Untrained<T, ST, U, SU, F, K>
    ): Mono<DlModel.Trained<T, ST, U, SU, F, K>> {
        TODO("Not yet implemented")
    }
}
