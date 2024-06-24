package com.arbr.data_common.base.pipeline

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.load.DataLoader
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.etl.transform.DataTransformer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Suppress("MemberVisibilityCanBePrivate")
internal class DataPipelineImpl<
        InpObj : DataRecordObject,
        InpGrp : RecordGrouping,
        OutObj : DataRecordObject,
        OutGrp : RecordGrouping,
        >(
    val extractor: DataExtractor<InpObj, InpGrp>,
    val transformer: DataTransformer<InpObj, InpGrp, OutObj, OutGrp>,
    val loader: DataLoader<OutObj, OutGrp>,
) : DataPipeline<InpObj, InpGrp, OutObj, OutGrp> {

    /**
     * Load objects from the source to the output sink and return them in a stream.
     */
    override fun loadAndGet(): Flux<RecordGroupDescribedPair<OutObj, OutGrp>> {
        val extractedElements = extractor.extract()
        val transformedElements = transformer.transform(extractedElements)
            .share()  // Replicate outputs across streams - TODO: test

        val loadFlux = loader
            .load(transformedElements)
            .flatMap {
                // This never gets hit, but it effectively performs a type conversion
                Mono.empty<RecordGroupDescribedPair<OutObj, OutGrp>>()
            }

        return Flux.merge(
            transformedElements,
            loadFlux,
        )
    }

    /**
     * Load objects from the source to the output sink and discard them from memory.
     */
    override fun load(): Flux<Void> {
        return extractor.extract()
            .run(transformer::transform)
            .run(loader::load) // Now that's what you call an ETL
    }


}