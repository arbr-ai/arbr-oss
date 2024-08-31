package com.arbr.object_model.functions.inference.embedding

import com.arbr.platform.object_graph.core.resource.field.ArbrFileContentValue
import com.arbr.platform.object_graph.core.resource.field.ArbrFileFilePathValue
import com.arbr.platform.object_graph.core.resource.field.ArbrFileSummaryValue
import com.arbr.platform.object_graph.common.model.collections.NestedObjectListType2
import com.arbr.platform.object_graph.common.values.SourcedValue
import com.arbr.platform.object_graph.file_system.VolumeState
import com.arbr.platform.ml.linear.typed.shape.Dim
import reactor.core.publisher.Mono

typealias FilePathsAndContentsValue = NestedObjectListType2.Value<String, String?, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue, ArbrFileContentValue>
typealias FilePathsAndSummariesValue = NestedObjectListType2.Value<String, String?, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue, ArbrFileSummaryValue>

/**
 * Placeholder to be replaced by the generic embedding RFunction helper
 */
interface FileEmbeddingSearchFunctions {
    fun <T : Any> filterRelevantItemsToTargetTokenCount(
        volumeState: VolumeState,
        sourcedQuery: SourcedValue<String>,
        items: List<T>,
        targetTokenCount: Int,
        serialize: (T) -> String,
    ): Mono<List<T>>

    fun embeddingSearchFilePathsAndSummariesValue(
        volumeState: VolumeState,
        sourcedQuery: SourcedValue<String>,
        filePathsAndSummariesValue: FilePathsAndSummariesValue,
        maxNumResults: Int,
        targetTokenCount: Int = 3000,
    ): Mono<FilePathsAndSummariesValue>

    fun embeddingSearchFilePathsAndContentsValue(
        volumeState: VolumeState,
        sourcedQuery: SourcedValue<String>,
        filePathsAndContentsValue: FilePathsAndContentsValue,
        maxNumResults: Int,
        targetTokenCount: Int = 3000,
    ): Mono<FilePathsAndContentsValue>
}