package com.arbr.data_common.base.pipeline

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import reactor.core.publisher.Flux


interface DataPipeline<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        OutObj : DataRecordObject,
        OutGrp : RecordGrouping,
        > {

    /**
     * Load objects from the source to the output sink and discard them from memory.
     */
    fun load(): Flux<Void>

    /**
     * Load objects from the source to the output sink and return them in a stream.
     */
    fun loadAndGet(): Flux<RecordGroupDescribedPair<OutObj, OutGrp>>

    companion object {

        fun newBuilder(): DataPipelineBuilder {
            return DataPipelineBuilder()
        }

    }

}
