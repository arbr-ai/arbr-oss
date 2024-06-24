package com.topdown.parsers.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.topdown.parsers.util.DocumentLocusHelper
import org.antlr.v4.runtime.RecognitionException

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class RecognitionExceptionModel(
    val className: String,
    val classSimpleName: String,
    val message: String?,
    val ruleContext: RuleContextModel?,
    val offendingToken: TokenModel?,
    val expectedTokens: List<Int>?,
) {

    companion object {
        fun fromRecognitionException(
            recognitionException: RecognitionException,
            documentLocusHelper: DocumentLocusHelper,
        ): RecognitionExceptionModel {
            return RecognitionExceptionModel(
                recognitionException::class.java.name,
                recognitionException::class.java.simpleName,
                recognitionException.message,
                recognitionException.ctx?.let { RuleContextModel.ofRuleContext(it, documentLocusHelper) },
                recognitionException.offendingToken?.let { TokenModel.fromToken(it, documentLocusHelper) },
                try {
                    recognitionException.expectedTokens?.toList()
                } catch (_: IllegalArgumentException) {
                    null
                },
            )
        }
    }
}