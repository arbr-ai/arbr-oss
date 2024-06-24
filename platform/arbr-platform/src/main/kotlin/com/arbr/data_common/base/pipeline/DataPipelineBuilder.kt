package com.arbr.data_common.base.pipeline

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.load.DataLoader
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.etl.transform.DataTransformer
import com.arbr.data_common.base.etl.transform.DataTransformerConfig
import com.arbr.data_common.base.functional.RecordGroupDescriptorTransformer
import com.arbr.data_common.base.functional.RecordGroupTransformer
import com.arbr.data_common.impl.etl.transform.DataTransformerImpl
import reactor.core.publisher.Flux

open class DataPipelineBuilder {


    /**
     * Step with Extractor
     *
     * Required - all others can be defaulted
     */
    open class WithExtractor<
            InpObj : DataRecordObject,
            InpGrp : RecordGrouping,
            >(
        private val extractor: DataExtractor<InpObj, InpGrp>,
    ) : DataPipelineBuilder() {

        private fun defaultTransformer(): DataTransformer<InpObj, InpGrp, InpObj, InpGrp> {
            val config = DataTransformerConfig()

            val identityDescriptorTransformer = RecordGroupDescriptorTransformer<InpObj, InpGrp, InpObj, InpGrp> {
                Flux.just(it)
            }
            val identityTransformer =
                RecordGroupTransformer<InpObj, InpGrp, InpObj, InpGrp> { recordGroup, descriptor, _ ->
                    Flux.just(
                        RecordGroupDescribedPair(recordGroup, descriptor)
                    )
                }

            return DataTransformerImpl(
                identityDescriptorTransformer,
                identityTransformer,
                config,
            )
        }

        private fun <
                OutObj : DataRecordObject,
                OutGrp : RecordGrouping,
                > withTransformer(
            transformer: DataTransformer<InpObj, InpGrp, OutObj, OutGrp>,
        ): WithTransformer<InpObj, InpGrp, OutObj, OutGrp> {
            return WithTransformer(
                extractor,
                transformer,
            )
        }

        /**
         * Skip adding a transformer and jump to adding the given loader.
         * This forces the input and output types to be the same.
         */
        fun withLoader(
            loader: DataLoader<InpObj, InpGrp>,
        ): WithLoader<InpObj, InpGrp, InpObj, InpGrp> {
            return withTransformer(defaultTransformer())
                .withLoader(loader)
        }

    }

    /**
     * Step with Transformer
     */
    open class WithTransformer<
            InpObj : DataRecordObject,
            InpGrp : RecordGrouping,
            OutObj : DataRecordObject,
            OutGrp : RecordGrouping,
            >(
        private val extractor: DataExtractor<InpObj, InpGrp>,
        private val transformer: DataTransformer<InpObj, InpGrp, OutObj, OutGrp>,
    ) : DataPipelineBuilder() {
        private fun defaultLoader(): DataLoader<OutObj, OutGrp> = TODO()

        fun withLoader(
            loader: DataLoader<OutObj, OutGrp>,
        ): WithLoader<InpObj, InpGrp, OutObj, OutGrp> {
            return WithLoader(
                extractor,
                transformer,
                loader
            )
        }

        fun build(): DataPipeline<InpObj,
                InpGrp,
                OutObj,
                OutGrp> {
            val defaultLoader: DataLoader<OutObj, OutGrp> = defaultLoader()
            val dataPipelineImpl: DataPipelineImpl<InpObj, InpGrp, OutObj, OutGrp> = DataPipelineImpl(
                extractor,
                transformer,
                defaultLoader,
            )

            return dataPipelineImpl
        }
    }

    /**
     * Step with Loader
     */
    open class WithLoader<
            InpObj : DataRecordObject,
            InpGrp : RecordGrouping,
            OutObj : DataRecordObject,
            OutGrp : RecordGrouping,
            >(
        private val extractor: DataExtractor<InpObj, InpGrp>,
        private val transformer: DataTransformer<InpObj, InpGrp, OutObj, OutGrp>,
        private val loader: DataLoader<OutObj, OutGrp>,
    ) : DataPipelineBuilder() {

        fun build(): DataPipeline<
                InpObj,
                InpGrp,
                OutObj,
                OutGrp,> {
            return DataPipelineImpl(
                extractor,
                transformer,
                loader,
            )
        }
    }

    fun <
            InpObj : DataRecordObject,
            InpGrp : RecordGrouping,
            > withExtractor(
        extractor: DataExtractor<InpObj, InpGrp>,
    ): WithExtractor<InpObj, InpGrp> {
        return WithExtractor(
            extractor,
        )
    }

}

