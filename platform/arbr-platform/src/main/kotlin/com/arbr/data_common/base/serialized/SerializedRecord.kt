package com.arbr.data_common.base.serialized

import com.arbr.data_common.base.format.DataRecordObjectFormat

interface SerializedRecord<Fmt: DataRecordObjectFormat> {

    val format: Fmt

    /**
     * Return the string content of the serialized record
     * Kind of a cop out for now, won't support binary very well
     * Not hard to support byte streams
     */
    fun getStringValue(): String

}

