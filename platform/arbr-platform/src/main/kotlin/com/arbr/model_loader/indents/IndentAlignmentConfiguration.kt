package com.arbr.model_loader.indents

import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.model.BindingParameter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IndentAlignmentConfiguration {

    @Bean(TRAINING_SCORE_ADMISSIBLE_THRESHOLD_NAME)
    fun trainingScoreAdmissibleThreshold(): BindingParameter<Double> {
        return BindingParameter(
            TRAINING_SCORE_ADMISSIBLE_THRESHOLD_METRIC,
            TRAINING_SCORE_ADMISSIBLE_THRESHOLD,
        )
    }


    companion object {
        const val TRAINING_SCORE_ADMISSIBLE_THRESHOLD_NAME = "topdown.models.indent.score-threshold"
        private const val TRAINING_SCORE_ADMISSIBLE_THRESHOLD = 0.8

        private val TRAINING_SCORE_ADMISSIBLE_THRESHOLD_METRIC = NamedMetricKind("TRAINING_SCORE_ADMISSIBLE_THRESHOLD")
    }
}
