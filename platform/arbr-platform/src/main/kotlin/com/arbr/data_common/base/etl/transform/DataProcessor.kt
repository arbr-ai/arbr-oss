package com.arbr.data_common.base.etl.transform

import reactor.core.publisher.Flux

interface DataProcessor {
    val enabled: Boolean

    fun process(): Flux<Void>
}