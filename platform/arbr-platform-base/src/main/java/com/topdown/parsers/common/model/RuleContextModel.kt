package com.topdown.parsers.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.topdown.parsers.util.DocumentLocusHelper
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class RuleContextModel(
    val ruleIndex: Int,
    val childCount: Int,

    // ParserRuleContext-specific fields
    val startToken: TokenModel?,
    val stopToken: TokenModel?,
) {

    companion object {
        fun ofRuleContext(
            ruleContext: RuleContext,
            documentLocusHelper: DocumentLocusHelper,
        ): RuleContextModel {
            return if (ruleContext is ParserRuleContext) {
                ofParserRuleContext(ruleContext, documentLocusHelper)
            } else {
                RuleContextModel(
                    ruleContext.ruleIndex,
                    ruleContext.childCount,
                    startToken = null,
                    stopToken = null,
                )
            }
        }

        private fun ofParserRuleContext(
            parserRuleContext: ParserRuleContext,
            documentLocusHelper: DocumentLocusHelper,
        ): RuleContextModel {
            return RuleContextModel(
                parserRuleContext.ruleIndex,
                parserRuleContext.childCount,
                startToken = parserRuleContext.start?.let { TokenModel.fromToken(it, documentLocusHelper) },
                stopToken = parserRuleContext.stop?.let { TokenModel.fromToken(it, documentLocusHelper) },
            )
        }
    }
}