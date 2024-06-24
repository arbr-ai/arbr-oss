package com.arbr.core_web_dev.service.embedding_search

//import com.arbr.content_formats.tokens.TokenizationUtils
//import com.arbr.engine.services.embedding.client.SimpleEmbeddingSearchClient
//import com.arbr.object_model.core.resource.field.ArbrFileContentValue
//import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
//import com.arbr.object_model.core.resource.field.ArbrFileSummaryValue
//import com.arbr.og.object_model.common.model.collections.NestedObjectListType2
//import com.arbr.og.object_model.common.values.SourcedValue
//import com.arbr.og_engine.file_system.VolumeState
//import com.arbr.platform.ml.linear.typed.shape.Dim
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import kotlin.math.ln
//import kotlin.math.sqrt
//
//typealias FilePathsAndContentsValue = NestedObjectListType2.Value<String, String?, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue, ArbrFileContentValue>
//typealias FilePathsAndSummariesValue = NestedObjectListType2.Value<String, String?, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue, ArbrFileSummaryValue>
//
//private inline fun <T> Iterable<T>.splitOn(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
//    val arrayListSatisfied = ArrayList<T>()
//    val arrayListUnsatisfied = ArrayList<T>()
//
//    for (element in this) if (predicate(element)) arrayListSatisfied.add(element) else arrayListUnsatisfied.add(element)
//
//    return arrayListSatisfied to arrayListUnsatisfied
//}
//
//@Component
//class FileEmbeddingSearchClient(
//    private val simpleEmbeddingSearchClient: SimpleEmbeddingSearchClient
//) {
//
//    private fun normalize(values: List<Double>): List<Double> {
//        if (values.isEmpty()) {
//            return emptyList()
//        }
//
//        val mean = values.average()
//        val squaredDifferences = values.sumOf { (it - mean) * (it - mean) }
//        val standardDeviation = sqrt(squaredDifferences / values.size)
//        if (standardDeviation == 0.0) {
//            return values.map { 0.0 }
//        }
//
//        return values.map { (it - mean) / standardDeviation }
//    }
//
//    fun <T : Any> filterRelevantItemsToTargetTokenCount(
//        volumeState: com.arbr.og_engine.file_system.VolumeState,
//        sourcedQuery: SourcedValue<String>,
//        items: List<T>,
//        targetTokenCount: Int,
//        serialize: (T) -> String,
//    ): Mono<List<T>> {
//        if (items.isEmpty() || targetTokenCount <= 0) {
//            return Mono.just(emptyList())
//        }
//
//        val serializedForms = items.map(serialize)
//        val tokenCounts = serializedForms.map { TokenizationUtils.getTokenCount(it) }
//        val normalizedTokenCounts = normalize(tokenCounts.map { it.toDouble() })
//
//        // Note no admissibility filter here
//        return simpleEmbeddingSearchClient.searchNearest(sourcedQuery.value, serializedForms, serializedForms.size)
//            .map { results ->
//                val (nearlyEqual, generalResults) = results.splitOn { it.embeddingDistance < 1E-2 }
//
//                val orderedNearlyEqualResults = nearlyEqual.sortedBy { tokenCounts[it.documentIndex] }
//
//                val orderedGeneralResults = if (generalResults.isNotEmpty()) {
//                    val normalizedInformationValues = normalize(generalResults.map { -ln(it.embeddingDistance) })
//                    val normalizedInformationValueMap = generalResults.withIndex().associate { (i, result) ->
//                        result.documentIndex to normalizedInformationValues[i]
//                    }
//
//                    nearlyEqual + generalResults.sortedByDescending { result ->
//                        val informationValueZ = normalizedInformationValueMap[result.documentIndex]!!
//                        val tokenCountZ = normalizedTokenCounts[result.documentIndex]
//
//                        informationValueZ - tokenCountZ
//                    }
//                } else {
//                    emptyList()
//                }
//
//                val orderedResults = orderedNearlyEqualResults + orderedGeneralResults
//
//                // Take while the cumulative token count is below the target
//                var cumulativeTokenCount = 0
//                val resultItems = mutableListOf<T>()
//
//                for (result in orderedResults) {
//                    val tokenCount = tokenCounts[result.documentIndex]
//                    if (cumulativeTokenCount + tokenCount <= targetTokenCount) {
//                        cumulativeTokenCount += tokenCount
//                        resultItems.add(items[result.documentIndex])
//                    }
//                }
//
//                resultItems
//            }
//    }
//
//    /**
//     * Shared utility among the XToRelevantFiles processors. Could be generalized more.
//     */
//    fun embeddingSearchFilePathsAndSummariesValue(
//        volumeState: VolumeState,
//        sourcedQuery: SourcedValue<String>,
//        filePathsAndSummariesValue: FilePathsAndSummariesValue,
//        maxNumResults: Int,
//        targetTokenCount: Int = 3000,
//    ): Mono<FilePathsAndSummariesValue> {
//        val fileInfoPairsIndexed = filePathsAndSummariesValue.value
//            .withIndex()
//            .mapNotNull { (i, v) ->
//                val path = v.t1
//
//                val summaryOpt = v.t2
//                summaryOpt?.let { summary ->
//
//
//                    i to (path to summary)
//                }
//            }
//
//        return Flux.fromIterable(fileInfoPairsIndexed)
//            .filterWhen { (_, p) ->
//                val (path, _) = p
//                com.arbr.engine.util.FileContentUtils.fileIsAdmissible(volumeState, path)
//            }
//            .collectList()
//            .flatMap { indexedFileInfoPairs ->
//                filterRelevantItemsToTargetTokenCount(
//                    volumeState,
//                    sourcedQuery,
//                    indexedFileInfoPairs,
//                    targetTokenCount
//                ) { (_, p) ->
//                    val (path, summary) = p
//                    "+++ $path\n$summary"
//                }
//                    .map { indexedFileInfoPairsFiltered ->
//                        val containers = filePathsAndSummariesValue.containers
//                        com.arbr.prompt_library.util.FilePathsAndSummaries.initializeMerged(
//                            indexedFileInfoPairsFiltered
//                                .take(maxNumResults)
//                                .map { containers[it.first] }
//                        )
//                    }
//            }
//    }
//
//    fun embeddingSearchFilePathsAndContentsValue(
//        volumeState: VolumeState,
//        sourcedQuery: SourcedValue<String>,
//        filePathsAndContentsValue: FilePathsAndContentsValue,
//        maxNumResults: Int,
//        targetTokenCount: Int = 3000,
//    ): Mono<FilePathsAndContentsValue> {
//        val fileInfoPairsIndexed = filePathsAndContentsValue.value
//            .withIndex()
//            .mapNotNull { (i, v) ->
//                val path = v.t1
//
//                val contentOpt = v.t2
//                contentOpt?.let { content ->
//
//
//                    i to (path to content)
//                }
//            }
//
//        return Flux.fromIterable(fileInfoPairsIndexed)
//            .filterWhen { (_, p) ->
//                val (path, _) = p
//                com.arbr.engine.util.FileContentUtils.fileIsAdmissible(volumeState, path)
//            }
//            .collectList()
//            .flatMap { indexedFileInfoPairs ->
//                filterRelevantItemsToTargetTokenCount(
//                    volumeState,
//                    sourcedQuery,
//                    indexedFileInfoPairs,
//                    targetTokenCount
//                ) { (_, p) ->
//                    val (path, content) = p
//                    "+++ $path\n$content"
//                }
//                    .map { indexedFileInfoPairsFiltered ->
//                        val containers = filePathsAndContentsValue.containers
//                        com.arbr.prompt_library.util.FilePathsAndContents.initializeMerged(
//                            indexedFileInfoPairsFiltered
//                                .take(maxNumResults)
//                                .map { containers[it.first] }
//                        )
//                    }
//            }
//    }
//
//}