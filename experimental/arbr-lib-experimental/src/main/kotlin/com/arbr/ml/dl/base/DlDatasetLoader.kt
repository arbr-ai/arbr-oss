package com.arbr.ml.dl.base

import com.arbr.platform.ml.linear.typed.base.*
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface DlDatasetLoader<ST: Shape, SU: Shape, F: GroundField<K>, K> {

    /**
     * Get batches of data as a Flux of 2-struct order+1 tensors of shape (BatchSize x ST) + (BatchSize x SU) ,
     * typically matrices.
     */
    fun <BatchSize: Dim> getBatches(): Flux<DlDataset<ST, SU, F, K, BatchSize>>

    /**
     * Get the whole dataset.
     */
    fun <Size: Dim> getDataset(): Mono<DlDataset<ST, SU, F, K, Size>>

}
