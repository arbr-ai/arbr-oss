package com.arbr.data_common.impl.serialized

import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.serialized.SerializedRecord

data class PlainStringSerializedRecord<Fmt: DataRecordObjectFormat>(
    override val format: Fmt,
    val value: String
): SerializedRecord<Fmt> {
    override fun getStringValue(): String {
        return value
    }
}