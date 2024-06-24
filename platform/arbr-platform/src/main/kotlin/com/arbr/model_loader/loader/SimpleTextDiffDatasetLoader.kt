package com.arbr.model_loader.loader

import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import org.springframework.stereotype.Component

@Component
class SimpleTextDiffDatasetLoader(
    private val noisyRecordLoader: NoisyRecordLoader,
) : DatasetLoader<GitHubPublicNoisedPatchInfo, DiffPatchTestCase> {
    override val datasetKind = DiffPatchDatasetKind.SIMPLE_TEXT_DIFF

    /**
     * Simple text diff loads from noisy and just uses the base / result values
     */
    override val recordLoader = noisyRecordLoader
    override val recordFilter = CleanDiffRecordFilter()
    override val recordMapper = SimpleTextDiffRecordMapper()
}