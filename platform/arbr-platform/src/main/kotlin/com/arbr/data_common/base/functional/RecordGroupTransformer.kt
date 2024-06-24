package com.arbr.data_common.base.functional

import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import reactor.core.publisher.Flux

fun interface RecordGroupTransformer<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        OutObj : DataRecordObject,
        OutGrp : RecordGrouping,
        > {

    fun transformRecordGroup(
        inputRecordGroup: RecordGroup<InpObj, InpGrp>,
        inputRecordGroupDescriptor: RecordGroupDescriptor<InpObj, InpGrp>,
        outputRecordGroupDescriptors: List<RecordGroupDescriptor<OutObj, OutGrp>>,
    ): Flux<RecordGroupDescribedPair<OutObj, OutGrp>>
}
