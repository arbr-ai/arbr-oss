package com.arbr.model_loader.loader

import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.load.DataLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
class ParameterLoaderFactory(
    @Qualifier("alignableWeightsProductionDataExtractor")
    private val alignableWeightsProductionDataExtractorMono: Mono<DataExtractor<SerializedScoredParameterSet, RecordGrouping.Single>>,
    @Qualifier("alignableWeightsDataExtractor")
    private val alignableWeightsDataExtractorMono: Mono<DataExtractor<SerializedScoredParameterSet, RecordGrouping.Single>>,

    /**
     * Loader sink for writing weights, if any
     */
    @Autowired(required = false)
    private val alignableWeightsDataLoaderMono: Mono<DataLoader<SerializedScoredParameterSet, RecordGrouping.Single>>?,
) {

    fun makeLoader(
        production: Boolean,
    ): Mono<ParameterLoader> {
        val dataExtractorMono =
            if (production) alignableWeightsProductionDataExtractorMono else alignableWeightsDataExtractorMono
        val dataLoaderMono = (alignableWeightsDataLoaderMono ?: Mono.empty())
            .map { Optional.of(it) }
            .defaultIfEmpty(Optional.empty())

        return Mono.zip(dataExtractorMono, dataLoaderMono).map { (dataExtractor, dataLoaderOpt) ->
            ParameterLoader(
                dataExtractor,
                dataLoaderOpt.getOrNull(),
            )
        }
    }
}
