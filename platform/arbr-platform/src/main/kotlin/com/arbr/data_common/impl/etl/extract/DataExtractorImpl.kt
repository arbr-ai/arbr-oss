package com.arbr.data_common.impl.etl.extract

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.extract.DataExtractorConfig
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.functional.DataExtractorFilter
import com.arbr.data_common.base.functional.DataRecordGroupDescriptorPaginator
import com.arbr.data_common.base.functional.DataRecordRetriever
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal class DataExtractorImpl<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        >(
    private val inputVolume: DataVolume,
    private val inputRecordCollection: DataRecordCollection<InpObj, InpGrp>,
    private val dataRecordGroupDescriptorPaginator: DataRecordGroupDescriptorPaginator<InpObj, InpGrp>,
    private val dataExtractorFilter: DataExtractorFilter<InpObj, InpGrp>,
    private val dataRecordRetriever: DataRecordRetriever<InpObj, InpGrp>,

    private val config: DataExtractorConfig = DataExtractorConfig(),
): DataExtractor<InpObj, InpGrp> {

    private fun extractResources(
        recordGroupDescriptor: RecordGroupDescriptor<InpObj, InpGrp>,
    ): Mono<RecordGroupDescribedPair<InpObj, InpGrp>> {
        val completeUri = recordGroupDescriptor.getCompleteUri(
            inputVolume,
            inputRecordCollection,
        )

        return dataRecordRetriever
            .retrieveRecordGroup(
                recordGroupDescriptor,
                completeUri,
            )
            .map { recordGroup ->
                RecordGroupDescribedPair(recordGroup, recordGroupDescriptor)
            }
    }

    override fun extract(): Flux<RecordGroupDescribedPair<InpObj, InpGrp>> {
        return dataRecordGroupDescriptorPaginator
            .loadRecordGroupDescriptors(
                config.manifestPageMaxSize,
                config.maxNumGroupDescriptors,
            )
            .filterWhen(dataExtractorFilter::shouldExtract, config.loadResourcesParallelism)
            .flatMap(this::extractResources, config.loadResourcesParallelism)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DataExtractor::class.java)
    }

}