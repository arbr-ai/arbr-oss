package com.arbr.model_loader.training.config_model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.arbr.util_common.hashing.HashUtils
import com.arbr.model_loader.model.DiffPatchTrainingStep

object TrainHardcodedConfiguration : TrainingConfiguration {
    private val mapper = jacksonObjectMapper()

    override val trainingSteps: List<DiffPatchTrainingStep> = listOf(
        TrainingConfigurations.trainHardcodedOnly,
    )

    override val sha: String = HashUtils.sha1Hash(
        *trainingSteps.map { mapper.writeValueAsString(it) }.toTypedArray()
    )
}