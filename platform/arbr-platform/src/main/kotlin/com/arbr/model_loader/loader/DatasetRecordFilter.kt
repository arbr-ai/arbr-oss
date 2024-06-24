package com.arbr.model_loader.loader

fun interface DatasetRecordFilter<Record> {
    fun shouldInclude(
        record: Record
    ): Boolean
}