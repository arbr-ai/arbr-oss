package com.arbr.data_common.base.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.serialized.SerializedRecord
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import reactor.core.publisher.Mono

fun interface DataRecordWriter<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        Fmt : DataRecordObjectFormat,
        Ser : SerializedRecord<Fmt>
        > {

    fun writeRecord(
        serializedRecordGroup: RecordGroup<Ser, Grp>,
        destinationUri: DataRecordFullyQualifiedUri,
    ): Mono<Void>
}
