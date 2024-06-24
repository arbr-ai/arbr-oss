package com.arbr.data_common.base.functional

import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.DataRecordObject
import reactor.core.publisher.Flux

fun interface RecordGroupDescriptorTransformer<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        OutObj : DataRecordObject,
        OutGrp : RecordGrouping,
        > {

    /**
     * Ideally we should be able to determine the output location(s) without actually loading the input
     */
    fun transformRecordGroupDescriptor(
        inputRecordGroupDescriptor: RecordGroupDescriptor<InpObj, InpGrp>,
    ): Flux<RecordGroupDescriptor<OutObj, OutGrp>>
}
