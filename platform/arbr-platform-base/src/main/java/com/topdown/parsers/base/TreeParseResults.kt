package com.topdown.parsers.base

import com.topdown.parsers.common.model.SyntaxErrorModel
import com.topdown.parsers.lang.universal.tree_parsing.ParserRuleTree
import org.antlr.v4.runtime.Token

data class TreeParseResults(
    val parserRuleTree: ParserRuleTree?,
    val syntaxErrors: List<SyntaxErrorModel>,
    val channelTokens: Map<Int, List<Token>>?,
)