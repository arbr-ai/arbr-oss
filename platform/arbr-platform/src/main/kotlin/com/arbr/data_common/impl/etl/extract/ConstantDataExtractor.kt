package com.arbr.data_common.impl.etl.extract

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.functional.DataRecordGroupDescriptorSpecifier
import com.arbr.data_common.impl.functional.DataRecordCollectionGroupInitializer
import reactor.core.publisher.Flux

class ConstantDataExtractor<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        >(
    private val grouping: InpGrp,
    private val datasetFlux: Flux<InpObj>,
    private val dataRecordGroupDescriptorSpecifier: DataRecordGroupDescriptorSpecifier<InpObj, InpGrp>,
) : DataExtractor<InpObj, InpGrp> {
    private val recordGroupInitializer: DataRecordCollectionGroupInitializer<InpObj, InpGrp> =
        DataRecordCollectionGroupInitializer.forGrouping(grouping)

    override fun extract(): Flux<RecordGroupDescribedPair<InpObj, InpGrp>> {
        return datasetFlux
            .map(recordGroupInitializer::makeRecordGroup)
            .flatMap { recordGroup ->
                dataRecordGroupDescriptorSpecifier
                    .specifyDescriptor(recordGroup)
                    .map { descriptor ->
                        RecordGroupDescribedPair(recordGroup, descriptor)
                    }
            }
    }

}
