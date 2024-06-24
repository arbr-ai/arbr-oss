package com.arbr.model_loader.model

data class DiffPatchTrainingStepSample(
    val datasetKind: DiffPatchDatasetKind,
    val numTrainingFiles: Int,
    val numTestFiles: Int,
)

sealed class DiffPatchTrainingStep(
    open val dataLoadingRandomSeed: Long,
    open val subsampleRate: Double,
    open val samples: List<DiffPatchTrainingStepSample>,
) {

    data class Train(
        val numEpochs: Int,
        override val dataLoadingRandomSeed: Long,
        val acceptThreshold: Double,
        val boundaryDiameterFinishThreshold: Double,
        override val subsampleRate: Double,
        override val samples: List<DiffPatchTrainingStepSample>,
    ): DiffPatchTrainingStep(
        dataLoadingRandomSeed,
        subsampleRate,
        samples,
    )

    data class Eval(
        override val dataLoadingRandomSeed: Long,
        override val samples: List<DiffPatchTrainingStepSample>,
    ): DiffPatchTrainingStep(
        dataLoadingRandomSeed,
        subsampleRate = 1.0,
        samples,
    )
}
