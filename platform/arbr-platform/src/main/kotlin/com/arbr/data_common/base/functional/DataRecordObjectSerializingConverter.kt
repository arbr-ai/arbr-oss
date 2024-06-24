package com.arbr.data_common.base.functional

import com.arbr.data_common.base.serialized.DataRecordMap
import com.arbr.data_common.base.DataRecordObject

fun interface DataRecordObjectSerializingConverter<
        Obj : DataRecordObject,
        > {

    /**
     * Convert record object to a map for serialized loading
     * Could be made more efficient by writing directly to buffers etc.
     */
    fun convertRecord(output: Obj): DataRecordMap
}
