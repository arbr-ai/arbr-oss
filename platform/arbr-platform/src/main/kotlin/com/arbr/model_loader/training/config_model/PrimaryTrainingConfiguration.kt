package com.arbr.model_loader.training.config_model

import com.arbr.model_loader.model.DiffPatchTrainingStep
import com.arbr.util_common.hashing.HashUtils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object PrimaryTrainingConfiguration : TrainingConfiguration {
    private val mapper = jacksonObjectMapper()

    override val trainingSteps: List<DiffPatchTrainingStep> = listOf(
        // 1 very coarse step
        TrainingConfigurations.train40Grit,
        // 1 medium coarse step
        TrainingConfigurations.train80Grit,
        // 1 more fine
        TrainingConfigurations.train220Grit,
        // 4 very fine
        TrainingConfigurations.train320Grit,
    )

    override val sha: String = HashUtils.sha1Hash(
        *trainingSteps.map { mapper.writeValueAsString(it) }.toTypedArray()
    )
}