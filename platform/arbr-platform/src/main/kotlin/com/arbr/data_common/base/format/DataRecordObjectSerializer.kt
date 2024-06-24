package com.arbr.data_common.base.format

import com.arbr.data_common.base.serialized.DataRecordMap
import com.arbr.data_common.base.serialized.SerializedRecord

interface DataRecordObjectSerializer<
        Fmt : DataRecordObjectFormat,
        Ser : SerializedRecord<Fmt>
        > {

    val format: Fmt

    /**
     * Serialize to in-memory record output format
     */
    fun serializeRecord(
        recordMap: DataRecordMap
    ): Ser
}
