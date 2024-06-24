package com.arbr.data_common.base.etl.extract

import com.arbr.data_common.base.etl.transform.DataProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class DbRecordIngestor<I>(
    override val enabled: Boolean,
) : DataProcessor {

    open val queryBatchSize: Int = QUERY_BATCH_SIZE_DEFAULT
    open val ingestEntryParallelism: Int = INGEST_PARALLELISM_DEFAULT

    abstract fun queryRecords(limit: Int): Flux<I>

    open fun shouldIncludeLoaded(parsedInput: I): Mono<Boolean> = Mono.just(true)

    abstract fun ingestEntry(entry: I): Mono<Void>

    protected fun ingest(): Flux<I> {
        return queryRecords(queryBatchSize)
            .filterWhen(this::shouldIncludeLoaded)
            .flatMap({
                if (it != null) {
                    ingestEntry(it)
                        .thenReturn(it)
                } else {
                    Mono.empty()
                }
            }, ingestEntryParallelism)
    }

    override fun process(): Flux<Void> {
        return ingest().flatMap { Mono.empty() }
    }

    companion object {
        private const val QUERY_BATCH_SIZE_DEFAULT = 100
        private const val INGEST_PARALLELISM_DEFAULT = 8
    }
}