package com.arbr.model_loader.loader

import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo

class NoisyDiffDatasetLoader(
    private val noisyRecordLoader: NoisyRecordLoader,
    override val datasetKind: DiffPatchDatasetKind,
) : DatasetLoader<GitHubPublicNoisedPatchInfo, DiffPatchTestCase> {
    private val datasetVersion: NoisyDiffDatasetVersion = when (datasetKind) {
        DiffPatchDatasetKind.SYNTHETIC_NOISE_V0 -> NoisyDiffDatasetVersion.V0
        DiffPatchDatasetKind.SYNTHETIC_NOISE_V1 -> NoisyDiffDatasetVersion.V1
        DiffPatchDatasetKind.SYNTHETIC_NOISE_V2 -> NoisyDiffDatasetVersion.V2
        DiffPatchDatasetKind.CLEAN,
        DiffPatchDatasetKind.SIMPLE_TEXT_DIFF,
        DiffPatchDatasetKind.HARDCODED -> throw Exception("Invalid dataset kind for ${this::class.java.simpleName}: ${datasetKind.serializedName}")
    }

    override val recordLoader = noisyRecordLoader
    override val recordFilter = NoisyDiffRecordFilter(datasetVersion)
    override val recordMapper = NoisyDiffRecordMapper(datasetKind)
}
