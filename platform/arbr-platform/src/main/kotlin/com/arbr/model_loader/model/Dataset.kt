package com.arbr.model_loader.model

import reactor.core.publisher.Flux

data class Dataset<T>(
    val name: String,
    val trainingData: Flux<T>,
    val testData: Flux<T>,
)
