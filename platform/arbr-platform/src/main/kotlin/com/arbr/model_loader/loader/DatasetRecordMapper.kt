package com.arbr.model_loader.loader

fun interface DatasetRecordMapper<Record, Datum> {
    fun mapRecord(
        record: Record,
    ): Datum
}