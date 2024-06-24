package com.arbr.model_loader.loader.config

import com.arbr.aws.s3.S3Service
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.extract.DataExtractorConfig
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.functional.DataExtractorFilter
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.impl.etl.extract.DataExtractorImpl
import com.arbr.data_common.impl.fetch.DataRecordGroupClient
import com.arbr.data_common.impl.fetch.DataRecordGroupClientFactory
import com.arbr.data_common.impl.functional.DataRecordGroupConstantFormatRecognizer
import com.arbr.data_common.impl.functional.DataRecordGroupSimpleExtensionFormatRecognizer
import com.arbr.data_common.impl.functional.DataRecordObjectParsingJacksonConverterFactory
import com.arbr.model_loader.loader.SerializedScoredParameterSet
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import com.arbr.model_loader.model.LanguageVocabularyWords
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

@Configuration
class DataExtractorConfig(
    s3Service: S3Service?,
    private val volumes: List<DataVolume>,
    @Qualifier("noisedPatchRecordCollection")
    private val noisedPatchRecordCollection: DataRecordCollection<GitHubPublicNoisedPatchInfo, RecordGrouping.Single>,
    @Qualifier("alignableWeightsProductionRecordCollection")
    private val alignableWeightsProductionRecordCollection: DataRecordCollection<SerializedScoredParameterSet, RecordGrouping.Single>,
    @Qualifier("alignableWeightsRecordCollection")
    private val alignableWeightsRecordCollection: DataRecordCollection<SerializedScoredParameterSet, RecordGrouping.Single>,
    @Qualifier("hardcodedDataRecordCollection")
    private val hardcodedDataRecordCollection: DataRecordCollection<DiffPatchTestCase, RecordGrouping.Single>,
    @Qualifier("documentModelVocabularyRecordCollection")
    private val documentModelVocabularyRecordCollection: DataRecordCollection<LanguageVocabularyWords, RecordGrouping.Single>,
) {
    private val dataRecordGroupClientFactory = DataRecordGroupClientFactory(s3Service)
    private val dataRecordConverterFactory = DataRecordObjectParsingJacksonConverterFactory.getInstance()

    private val clientCache = ConcurrentHashMap<Pair<String, String>, Optional<DataRecordGroupClient<*, *, *>>>()

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > dataRecordGroupFormatRecognizer(): DataRecordGroupFormatRecognizer<Obj, Grp> {
        // Use a simple extension recognizer for now with JSON fallback
        return DataRecordGroupFormatRecognizer.coalesce(
            DataRecordGroupSimpleExtensionFormatRecognizer(),
            DataRecordGroupConstantFormatRecognizer(DataRecordObjectFormat.FileJson),
        )
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > getClient(
        dataVolume: DataVolume,
        dataRecordCollection: DataRecordCollection<Obj, Grp>,
    ): DataRecordGroupClient<*, Obj, Grp>? {
        @Suppress("UNCHECKED_CAST")
        return clientCache.computeIfAbsent(dataVolume.relativeId to dataRecordCollection.relativeId) {
            Optional.ofNullable(
                dataRecordGroupClientFactory.makeClient(
                    dataVolume,
                    dataRecordCollection,
                    dataRecordGroupFormatRecognizer(),
                    dataRecordConverterFactory.makeConverter(dataRecordCollection.recordObjectClass),
                )
            )
        }.getOrNull()?.let {
            it as DataRecordGroupClient<*, Obj, Grp>
        }
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > selectVolumeForRecordCollection(
        dataRecordCollection: DataRecordCollection<Obj, Grp>,
    ): Mono<DataVolume> {
        // Find the volume of greatest priority with nonempty contents
        return Flux.fromIterable(volumes)
            .filterWhen { dataVolume: DataVolume ->
                val client = getClient(dataVolume, dataRecordCollection)
                val includeMono = client?.isEmpty()?.map { !it } ?: Mono.just(false)

                includeMono
                    .doOnNext { include ->
                        if (include) {
                            logger.debug("Including volume ${dataVolume.uriComponent.uri} as candidate for ${dataRecordCollection.uriComponent.uriComponent} - $client")
                        } else {
                            logger.debug("Excluding volume ${dataVolume.uriComponent.uri} as candidate for ${dataRecordCollection.uriComponent.uriComponent} - $client")
                        }
                    }
            }
            .collectList()
            .mapNotNull { validVolumes ->
                validVolumes.minByOrNull { it.priority }
            }
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > getBestDataExtractor(
        recordCollection: DataRecordCollection<Obj, Grp>
    ): Mono<DataExtractor<Obj, Grp>> {
        return selectVolumeForRecordCollection(recordCollection)
            .map { dataVolume ->
                // Client must exist if selected
                val client = getClient(dataVolume, recordCollection)!!

                DataExtractorImpl(
                    dataVolume,
                    recordCollection,
                    client,
                    DataExtractorFilter.passthrough(), // TODO: Allow specifying filter on record collection in spec
                    client,
                )
            }
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > loadBestDataExtractor(
        recordCollection: DataRecordCollection<Obj, Grp>
    ): Mono<DataExtractor<Obj, Grp>> {
        return getBestDataExtractor(recordCollection)
            .cache()
            .also { it.subscribe() }  // Begin loading immediately
    }

    @Bean("noisedPatchDataExtractor")
    fun noisedPatchDataExtractor(): Mono<DataExtractor<GitHubPublicNoisedPatchInfo, RecordGrouping.Single>> {
        return loadBestDataExtractor(noisedPatchRecordCollection)
    }

    @Bean("alignableWeightsProductionDataExtractor")
    fun alignableWeightsProductionDataExtractor(): Mono<DataExtractor<SerializedScoredParameterSet, RecordGrouping.Single>> {
        return loadBestDataExtractor(alignableWeightsProductionRecordCollection)
    }

    @Bean("alignableWeightsDataExtractor")
    fun alignableWeightsDataExtractor(): Mono<DataExtractor<SerializedScoredParameterSet, RecordGrouping.Single>> {
        return loadBestDataExtractor(alignableWeightsRecordCollection)
    }

    @Bean("hardcodedDataExtractor")
    fun hardcodedDataExtractor(): Mono<DataExtractor<DiffPatchTestCase, RecordGrouping.Single>> {
        return loadBestDataExtractor(hardcodedDataRecordCollection)
    }

    @Bean("documentModelVocabularyDataExtractor")
    fun documentModelVocabularyDataExtractor(): Mono<DataExtractor<LanguageVocabularyWords, RecordGrouping.Single>> {
        return loadBestDataExtractor(documentModelVocabularyRecordCollection)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DataExtractorConfig::class.java)
    }
}
