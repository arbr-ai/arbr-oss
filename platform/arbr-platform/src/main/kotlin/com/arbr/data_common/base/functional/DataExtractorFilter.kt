package com.arbr.data_common.base.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import reactor.core.publisher.Mono
import reactor.kotlin.extra.bool.not

fun interface DataExtractorFilter<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        > {

    fun shouldExtract(
        recordGroupDescriptor: RecordGroupDescriptor<InpObj, InpGrp>,
    ): Mono<Boolean>

    private class OutputNonexistenceFilter<
            InpObj : DataRecordObject,
            InpGrp : RecordGrouping,
            OutObj : DataRecordObject,
            OutGrp : RecordGrouping,
            >(
        private val recordGroupDescriptorTransformer: RecordGroupDescriptorTransformer<InpObj, InpGrp, OutObj, OutGrp>,
    ) : DataExtractorFilter<InpObj, InpGrp> {
        private fun recordExists(
            outputRecordGroupDescriptor: RecordGroupDescriptor<OutObj, OutGrp>,
        ): Mono<Boolean> {
            return Mono.just(false) // TODO
        }

        override fun shouldExtract(
            recordGroupDescriptor: RecordGroupDescriptor<InpObj, InpGrp>,
        ): Mono<Boolean> {
            return recordGroupDescriptorTransformer
                .transformRecordGroupDescriptor(recordGroupDescriptor)
                .filterWhen {
                    recordExists(it).not()
                }
                .collectList()
                .map { it.isNotEmpty() }
        }
    }

    companion object {

        fun <
                InpObj : DataRecordObject,
                InpGrp : RecordGrouping,
                > passthrough(): DataExtractorFilter<InpObj, InpGrp> {
            return DataExtractorFilter {
                Mono.just(true)
            }
        }

        fun <
                InpObj : DataRecordObject,
                InpGrp : RecordGrouping,
                OutObj : DataRecordObject,
                OutGrp : RecordGrouping,
                > ignoringExistingOutputs(
            recordGroupDescriptorTransformer: RecordGroupDescriptorTransformer<InpObj, InpGrp, OutObj, OutGrp>,
        ): DataExtractorFilter<InpObj, InpGrp> {
            return OutputNonexistenceFilter(recordGroupDescriptorTransformer)
        }

    }
}