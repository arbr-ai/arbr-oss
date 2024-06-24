package com.arbr.model_loader.training.config_model

import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTrainingStep
import com.arbr.model_loader.model.DiffPatchTrainingStepSample

internal object TrainingConfigurations {
    private const val NUM_HARDCODED_FILES = 8

    val evalHardcodedOnly = DiffPatchTrainingStep.Eval(
        120210313L,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.HARDCODED,
                NUM_HARDCODED_FILES,
                NUM_HARDCODED_FILES,
            ),
        ),
    )

    val trainHardcodedOnly = DiffPatchTrainingStep.Train(
        100,
        210199911L,
        0.85,
        0.01,
        1.0,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.HARDCODED,
                1000,
                NUM_HARDCODED_FILES,
            ),
        ),
    )

    val trainSimpleTextInit = DiffPatchTrainingStep.Train(
        12,
        120210313L,
        0.65,
        0.05,
        0.1,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.SIMPLE_TEXT_DIFF,
                400,
                50,
            ),
        ),
    )

    val trainSimpleText1 = DiffPatchTrainingStep.Train(
        4,
        121010313L,
        0.85,
        0.05,
        0.8,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.SIMPLE_TEXT_DIFF,
                20,
                5,
            ),
        ),
    )

    val trainSimpleText2 = DiffPatchTrainingStep.Train(
        12,
        1011210313L,
        0.99,
        0.02,
        0.15,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.SIMPLE_TEXT_DIFF,
                1000,
                200,
            ),
        ),
    )

    val train40Grit = DiffPatchTrainingStep.Train(
        1,
        120210313L,
        0.81,
        0.05,
        1.0,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.HARDCODED,
                NUM_HARDCODED_FILES,
                NUM_HARDCODED_FILES,
            ),
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.CLEAN,
                4,
                4,
            ),
        ),
    )
    val train80Grit = DiffPatchTrainingStep.Train(
        1,
        111897412L,
        0.85,
        0.04,
        0.8,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.HARDCODED,
                2 * NUM_HARDCODED_FILES, // Hack to double-weight these
                2 * NUM_HARDCODED_FILES,
            ),
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.CLEAN,
                10,
                5,
            ),
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.SYNTHETIC_NOISE_V2,
                10,
                5,
            ),
        ),
    )
    val train220Grit = DiffPatchTrainingStep.Train(
        1,
        123897412L,
        0.9,
        0.01,
        0.8,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.HARDCODED,
                2 * NUM_HARDCODED_FILES, // Hack to double-weight these
                2 * NUM_HARDCODED_FILES,
            ),
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.CLEAN,
                15,
                8,
            ),
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.SYNTHETIC_NOISE_V2,
                15,
                8,
            ),
        ),
    )
    val train320Grit = DiffPatchTrainingStep.Train(
        4,
        123897412L,
        0.95,
        0.001, // Tighter bound
        1.0,
        listOf(
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.HARDCODED,
                2 * NUM_HARDCODED_FILES, // Hack to double-weight these
                2 * NUM_HARDCODED_FILES,
            ),
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.CLEAN,
                100,
                10,
            ),
            DiffPatchTrainingStepSample(
                DiffPatchDatasetKind.SYNTHETIC_NOISE_V2,
                1000,
                45,
            ),
        ),
    )
}