package com.arbr.model_loader.loader

import com.arbr.content_formats.mapper.Mappers
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.load.DataLoader
import com.arbr.data_common.base.functional.DataRecordGroupDescriptorSpecifier
import com.arbr.data_common.base.pipeline.DataPipelineBuilder
import com.arbr.data_common.impl.etl.extract.ConstantDataExtractor
import com.arbr.data_common.impl.files.DataRecordGroupDescriptorFactory
import com.arbr.util_common.hashing.HashUtils
import com.arbr.ml.math.model.RationalValue
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.model.BindingParameter
import com.arbr.ml.optimization.model.ScoredParameterSet
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Paths
import java.time.Instant
import java.util.*

class ParameterLoader(
    private val dataExtractor: DataExtractor<SerializedScoredParameterSet, RecordGrouping.Single>,
    private val dataLoader: DataLoader<SerializedScoredParameterSet, RecordGrouping.Single>?,
) {

    // val key = "com.arbr.alignable-weights/${parameterPath.fileName}"
    private val sessionId = UUID.randomUUID().toString().takeLast(12)
    private val weightFileNameRegex = Regex("weights_.*\\.json")

    private fun quantize(parameters: Map<NamedMetricKind, BindingParameter<Double>>): Map<NamedMetricKind, BindingParameter<RationalValue>> {
        return parameters.mapValues { (_, bp) ->
            BindingParameter(
                metricKind = bp.metricKind,
                value = RationalValue.ofDouble(bp.value)
            )
        }
    }

    private fun unQuantize(parameters: Map<NamedMetricKind, BindingParameter<RationalValue>>): Map<NamedMetricKind, BindingParameter<Double>> {
        return parameters.mapValues { (_, bp) ->
            BindingParameter(
                metricKind = bp.metricKind,
                value = bp.value.doubleValue,
            )
        }
    }

    private fun hashParameters(quantizedParameters: Map<NamedMetricKind, BindingParameter<RationalValue>>): String {
        val serialized = writer.writeValueAsString(quantizedParameters).trimEnd() + "\n"
        return HashUtils.sha1Hash(serialized)
    }

    private fun serializedForm(scoredParameterSet: ScoredParameterSet): SerializedScoredParameterSet {
        val quantizedParameters = quantize(scoredParameterSet.parameters)
        val sha = hashParameters(quantizedParameters)
        return SerializedScoredParameterSet(
            sha,
            scoredParameterSet.name,
            quantizedParameters.values.toList(),
            RationalValue.ofDouble(scoredParameterSet.trainingScore),
            RationalValue.ofDouble(scoredParameterSet.testScore),
        )
    }

    private fun parsedForm(serializedScoredParameterSet: SerializedScoredParameterSet): ScoredParameterSet {
        val quantizedParameters =
            unQuantize(serializedScoredParameterSet.parameters.associateBy { b -> b.metricKind })
        return ScoredParameterSet(
            serializedScoredParameterSet.name,
            quantizedParameters,
            serializedScoredParameterSet.trainingScore.doubleValue,
            serializedScoredParameterSet.testScore.doubleValue,
        )
    }

    private val descriptorFactory = DataRecordGroupDescriptorFactory()
    private val dataRecordGroupDescriptorSpecifier =
        DataRecordGroupDescriptorSpecifier<SerializedScoredParameterSet, RecordGrouping.Single> { recordGroup ->
            dataLoader?.let { dataLoader ->
                val serializedParameterSet = recordGroup.flatten().first()
                val parameterHash = hashParameters(serializedParameterSet.parameters.associateBy { b -> b.metricKind })
                val nowMs = Instant.now().toEpochMilli()
                val fileSubPath = Paths.get(sessionId, "weights_${nowMs}_$parameterHash.json")

                val dataVolume = dataLoader.outputVolume
                val recordCollection = dataLoader.outputRecordCollection
                Mono.just(
                    descriptorFactory.makeDescriptor(
                        dataVolume,
                        recordCollection,
                        fileSubPath,
                    )
                )
            } ?: Mono.empty()
        }

    private fun innerPersistParameterSet(
        parameterSet: ScoredParameterSet,
    ): Mono<Void> {
        return dataLoader?.let { dataLoader ->
            val serializedParameterSet = serializedForm(parameterSet)

            val dataExtractor = ConstantDataExtractor(
                RecordGrouping.Single,
                Flux.just(serializedParameterSet),
                dataRecordGroupDescriptorSpecifier,
            )
            val dataPipeline = DataPipelineBuilder()
                .withExtractor(dataExtractor)
                .withLoader(dataLoader)
                .build()

            dataPipeline.load().then()
        } ?: Mono.empty()
    }

    fun loadParameterSet(): Mono<ScoredParameterSet> {
        return dataExtractor
            .extract()
            .flatMapIterable { it.recordGroup.flatten() }
            .take(128L)
            .map(this::parsedForm)
            .collectList()
            .mapNotNull { parameterSets ->
                parameterSets.maxByOrNull { it.testScore }
            }
    }

    fun persistParameterSet(
        parameterSet: ScoredParameterSet,
    ): Mono<Void> {
        return innerPersistParameterSet(parameterSet)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ParameterLoader::class.java)
        private val mapper = Mappers.mapper
        private val writer = mapper.writerWithDefaultPrettyPrinter()
    }

}
