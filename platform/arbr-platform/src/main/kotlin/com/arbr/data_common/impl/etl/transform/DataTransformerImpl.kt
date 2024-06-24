package com.arbr.data_common.impl.etl.transform

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.etl.transform.DataTransformer
import com.arbr.data_common.base.etl.transform.DataTransformerConfig
import com.arbr.data_common.base.functional.RecordGroupDescriptorTransformer
import com.arbr.data_common.base.functional.RecordGroupTransformer
import reactor.core.publisher.Flux

class DataTransformerImpl<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        OutObj : DataRecordObject,
        OutGrp : RecordGrouping,
        >(
    private val recordGroupDescriptorTransformer: RecordGroupDescriptorTransformer<
            InpObj, InpGrp, OutObj, OutGrp
            >,
    private val recordGroupTransformer: RecordGroupTransformer<
            InpObj, InpGrp, OutObj, OutGrp
            >,
    private val dataTransformerConfig: DataTransformerConfig = DataTransformerConfig(),
) : DataTransformer<InpObj, InpGrp, OutObj, OutGrp> {

    private fun innerTransform(
        inputRecordGroup: RecordGroup<InpObj, InpGrp>,
        inputRecordGroupDescriptor: RecordGroupDescriptor<InpObj, InpGrp>,
    ): Flux<RecordGroupDescribedPair<OutObj, OutGrp>> {
        return recordGroupDescriptorTransformer
            .transformRecordGroupDescriptor(inputRecordGroupDescriptor)
            .collectList()
            .flatMapMany { outputDescriptors ->
                recordGroupTransformer.transformRecordGroup(
                    inputRecordGroup,
                    inputRecordGroupDescriptor,
                    outputDescriptors,
                )
            }
    }

    override fun transform(
        inputElements: Flux<RecordGroupDescribedPair<InpObj, InpGrp>>,
    ): Flux<RecordGroupDescribedPair<OutObj, OutGrp>> {
        return inputElements
            .flatMap({ recordGroupDescribedPair ->
                innerTransform(
                    recordGroupDescribedPair.recordGroup,
                    recordGroupDescribedPair.recordGroupDescriptor,
                )
            }, dataTransformerConfig.transformParallelism)
    }
}