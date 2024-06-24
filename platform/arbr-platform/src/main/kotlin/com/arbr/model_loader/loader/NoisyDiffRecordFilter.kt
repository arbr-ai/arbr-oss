package com.arbr.model_loader.loader

import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import org.apache.commons.text.similarity.LevenshteinDistance
import org.slf4j.LoggerFactory

class NoisyDiffRecordFilter(
    private val datasetVersion: NoisyDiffDatasetVersion
) : DatasetRecordFilter<GitHubPublicNoisedPatchInfo> {
    private val levenshteinDistance = LevenshteinDistance.getDefaultInstance()

    private val textLengthLimit = 4000

    /**
     * Minimum Levenshtein distance between noised patch and clean patch to qualify.
     */
    private val minEditDistanceBetweenPatches = 100

    override fun shouldInclude(record: GitHubPublicNoisedPatchInfo): Boolean {
        val recordDatasetVersion = NoisyDiffDatasetVersion.values().firstOrNull {
            it.serializedName == record.noiseModelVersion
        }

        if (recordDatasetVersion == null) {
            logger.info("Unrecognized dataset version on record: ${record.noiseModelVersion}")
            return false
        }

        return (
                recordDatasetVersion == datasetVersion
                        && record.baseDocument.length < textLengthLimit
                        && record.fileContent.length < textLengthLimit
                        && record.patchContent.length < textLengthLimit
                        && levenshteinDistance.apply(
                    record.patchContent,
                    record.patchContentWithNoise
                ) >= minEditDistanceBetweenPatches
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NoisyDiffRecordFilter::class.java)
    }
}