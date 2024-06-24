package com.arbr.data_common.base.etl.transform

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import reactor.core.publisher.Flux

fun interface DataTransformer<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        OutObj : DataRecordObject,
        OutGrp : RecordGrouping,
        > {
    fun transform(
        inputElements: Flux<RecordGroupDescribedPair<InpObj, InpGrp>>,
    ): Flux<RecordGroupDescribedPair<OutObj, OutGrp>>

    companion object {
        fun <
                Obj : DataRecordObject,
                Grp : RecordGrouping,
                > identity(): DataTransformer<Obj, Grp, Obj, Grp> {
            return DataTransformer { it }
        }
    }
}
