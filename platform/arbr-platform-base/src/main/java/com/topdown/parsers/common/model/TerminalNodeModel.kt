package com.topdown.parsers.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.topdown.parsers.util.DocumentLocusHelper
import org.antlr.v4.runtime.tree.TerminalNode

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TerminalNodeModel(
    val tokenModel: TokenModel,
    val isError: Boolean,
) {

    companion object {
        fun fromTerminalNode(
            terminalNode: TerminalNode,
            isError: Boolean,
            documentLocusHelper: DocumentLocusHelper,
        ): TerminalNodeModel {
            return TerminalNodeModel(
                TokenModel.fromToken(terminalNode.symbol, documentLocusHelper),
                isError,
            )
        }
    }
}
