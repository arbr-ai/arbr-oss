package com.arbr.data_common.base.etl.extract

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import reactor.core.publisher.Flux

interface DataExtractor<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        > {

    fun extract(): Flux<RecordGroupDescribedPair<InpObj, InpGrp>>

}
