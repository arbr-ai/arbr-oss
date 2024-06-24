package com.arbr.model_loader.loader

import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class NoisyRecordLoader(
    @Qualifier("noisedPatchDataExtractor")
    private val dataExtractorMono: Mono<DataExtractor<GitHubPublicNoisedPatchInfo, RecordGrouping.Single>>,
) : DatasetRecordLoader<GitHubPublicNoisedPatchInfo> {

    /**
     * Folder within bucket
     */
    private val s3Folder = "com.arbr.alignable-diffs/noised/0/"

    override fun loadRecords(
        recordFilter: DatasetRecordFilter<GitHubPublicNoisedPatchInfo>,
        manifestFileName: String,
        numFiles: Int,
        random: Random
    ): Flux<GitHubPublicNoisedPatchInfo> {
        return dataExtractorMono
            .flatMapMany { dataExtractor ->
                dataExtractor
                    .extract()
            }
            .flatMapIterable {
                it.recordGroup.flatten()
            }
            .filter(recordFilter::shouldInclude)
            .take(numFiles.toLong())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NoisyRecordLoader::class.java)
    }
}