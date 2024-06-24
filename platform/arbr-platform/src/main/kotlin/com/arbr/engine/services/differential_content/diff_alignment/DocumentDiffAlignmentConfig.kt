package com.arbr.engine.services.differential_content.diff_alignment

import com.arbr.model_loader.loader.ParameterLoaderFactory
import com.arbr.model_suite.parameters.ParameterValueProviderImpl
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentHelper
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentHelperDeferredImpl
import com.arbr.ml.optimization.base.ParameterValueProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class DocumentDiffAlignmentConfig(
    private val parameterLoaderFactory: ParameterLoaderFactory,
    @Value("\${arbr.diff-alignment.parameter-default:1.0}")
    private val parameterDefaultValue: Double,
) {
    private val parameterLoaderMono = parameterLoaderFactory.makeLoader(
        production = true,
    )

    @Bean
    fun documentDiffAlignmentHelper(): DocumentDiffAlignmentHelper {
        val parameterValueProviderMono: Mono<ParameterValueProvider> = parameterLoaderMono.flatMap { parameterLoader ->
            parameterLoader.loadParameterSet().map { scoredParameterSet ->
                val parameterMap = scoredParameterSet.parameters
                ParameterValueProviderImpl(
                    parameterMap,
                    defaultValue = parameterDefaultValue,
                )
            }
        }

        return DocumentDiffAlignmentHelperDeferredImpl(
            parameterValueProviderMono
        )
    }

}
