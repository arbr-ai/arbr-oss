package com.arbr.model_loader.training.config_model

import com.arbr.util_common.hashing.HashUtils
import com.arbr.model_loader.model.DiffPatchTrainingStep
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object SingularTrainingConfiguration : TrainingConfiguration {
    private val mapper = jacksonObjectMapper()

    override val trainingSteps: List<DiffPatchTrainingStep> = listOf(
        TrainingConfigurations.train320Grit,
    )

    override val sha: String = HashUtils.sha1Hash(
        *trainingSteps.map { mapper.writeValueAsString(it) }.toTypedArray()
    )
}