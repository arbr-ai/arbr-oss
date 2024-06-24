package com.arbr.model_loader.indents

import com.arbr.content_formats.format.tokenizer.TokenFormatter
import com.arbr.data_structures_common.partial_order.PartialOrder

fun interface TokenFormatterCompiler<DocumentType, TokenType> {

    fun compileFormatter(
        tokens: PartialOrder<TokenType>,
        preSerializedTargetDocument: DocumentType,
    ): TokenFormatter<TokenType>

    companion object {
        fun <D, T> plain(): TokenFormatterCompiler<D, T> {
            return TokenFormatterCompiler { _, _ ->
                TokenFormatter.plain()
            }
        }
    }
}
