package com.topdown.parsers.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GeneralParseTreeNodeModel(
    val pushIndex: Int,
    val popIndex: Int?,
    val parentPushIndex: Int?,
    val ruleContextModel: RuleContextModel?,
    val terminalNodeModel: TerminalNodeModel?,
)