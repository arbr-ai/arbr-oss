package com.arbr.model_loader.training.config_model

import com.arbr.model_loader.model.DiffPatchTrainingStep


interface TrainingConfiguration {
    /**
     * A SHA-1 hash identifying the configuration.
     */
    val sha: String

    /**
     * Steps for training.
     */
    val trainingSteps: List<DiffPatchTrainingStep>
}
