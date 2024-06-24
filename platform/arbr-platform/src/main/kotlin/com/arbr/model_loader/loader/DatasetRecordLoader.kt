package com.arbr.model_loader.loader

import reactor.core.publisher.Flux
import java.util.*

fun interface DatasetRecordLoader<Record> {
    fun loadRecords(
        recordFilter: DatasetRecordFilter<Record>,
        manifestFileName: String,
        numFiles: Int,
        random: Random,
    ): Flux<Record>
}
