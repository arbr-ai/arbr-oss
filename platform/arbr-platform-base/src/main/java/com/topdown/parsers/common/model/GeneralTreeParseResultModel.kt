package com.topdown.parsers.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GeneralTreeParseResultModel(
    val nodeList: List<GeneralParseTreeNodeModel>,
    val syntaxErrors: List<SyntaxErrorModel>,
    val channelTokens: Map<Int, List<TokenModel>>?,

    /**
     * Map of ruleIndex to fully qualified class name encountered during parsing.
     */
    val ruleClassNames: Map<Int, String>,
)