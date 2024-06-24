package com.topdown.parsers.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.topdown.parsers.util.DocumentLocusHelper
import org.antlr.v4.runtime.Token

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenModel(
    /*
       Directly from Antlr's Token.java
     */

    val type: Int,
    val text: String,
    val line: Int,
    val charPositionInLine: Int,
    val channel: Int,
    val tokenIndex: Int,
    val startIndex: Int,
    val stopIndex: Int,

    /*
       Custom fields
     */
    val locus: Int?

) {

    companion object {
        fun fromToken(
            token: Token,
            documentLocusHelper: DocumentLocusHelper,
        ): TokenModel {
            return TokenModel(
                token.type,
                token.text,
                token.line,
                token.charPositionInLine,
                token.channel,
                token.tokenIndex,
                token.startIndex,
                token.stopIndex,
                documentLocusHelper.computeLocus(token.line, token.charPositionInLine),
            )
        }
    }
}
