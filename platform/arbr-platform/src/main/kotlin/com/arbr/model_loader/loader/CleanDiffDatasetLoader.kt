package com.arbr.model_loader.loader

import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import org.springframework.stereotype.Component

@Component
class CleanDiffDatasetLoader(
    private val noisyRecordLoader: NoisyRecordLoader,
) : DatasetLoader<GitHubPublicNoisedPatchInfo, DiffPatchTestCase> {
    override val datasetKind = DiffPatchDatasetKind.CLEAN

    /**
     * Clean diff loads from noisy and just uses the base / clean patch / result values
     */
    override val recordLoader = noisyRecordLoader
    override val recordFilter = CleanDiffRecordFilter()
    override val recordMapper = CleanDiffRecordMapper()
}

