package com.arbr.object_model.functions.inference.embedding

import com.arbr.platform.object_graph.common.values.SourcedValue
import com.arbr.platform.object_graph.file_system.VolumeState
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Placeholder to be replaced by the generic embedding RFunction helper
 */
@Component
class FileEmbeddingSearchFunctionsImpl: FileEmbeddingSearchFunctions {
    override fun embeddingSearchFilePathsAndContentsValue(
        volumeState: VolumeState,
        sourcedQuery: SourcedValue<String>,
        filePathsAndContentsValue: FilePathsAndContentsValue,
        maxNumResults: Int,
        targetTokenCount: Int
    ): Mono<FilePathsAndContentsValue> {
        TODO("Not yet implemented")
    }

    override fun embeddingSearchFilePathsAndSummariesValue(
        volumeState: VolumeState,
        sourcedQuery: SourcedValue<String>,
        filePathsAndSummariesValue: FilePathsAndSummariesValue,
        maxNumResults: Int,
        targetTokenCount: Int
    ): Mono<FilePathsAndSummariesValue> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> filterRelevantItemsToTargetTokenCount(
        volumeState: VolumeState,
        sourcedQuery: SourcedValue<String>,
        items: List<T>,
        targetTokenCount: Int,
        serialize: (T) -> String
    ): Mono<List<T>> {
        TODO("Not yet implemented")
    }
}
