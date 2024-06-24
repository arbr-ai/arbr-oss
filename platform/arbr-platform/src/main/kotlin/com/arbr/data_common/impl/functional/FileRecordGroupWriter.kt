package com.arbr.data_common.impl.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.functional.DataRecordWriter
import com.arbr.data_common.impl.serialized.PlainStringSerializedRecord
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.io.path.toPath
import kotlin.io.path.writeText

class FileRecordGroupWriter<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        Fmt : DataRecordObjectFormat,
        Ser : PlainStringSerializedRecord<Fmt>
        > : DataRecordWriter<Obj, Grp, Fmt, Ser> {

    override fun writeRecord(
        serializedRecordGroup: RecordGroup<Ser, Grp>,
        destinationUri: DataRecordFullyQualifiedUri
    ): Mono<Void> {
        return Mono.fromCallable {
            val uri = destinationUri.concat().toUri()
            val outPath = uri.toPath()

            // Assumes line-delimited groups
            val serializedText = when (serializedRecordGroup) {
                is RecordGroup.Batch -> serializedRecordGroup.records.joinToString("\n") { it.getStringValue() }
                is RecordGroup.BatchFixed -> serializedRecordGroup.records.joinToString("\n") { it.getStringValue() }
                is RecordGroup.Single -> serializedRecordGroup.record.getStringValue()
            }

            outPath.writeText(serializedText)
        }
            .subscribeOn(Schedulers.boundedElastic())
            .then()
    }
}