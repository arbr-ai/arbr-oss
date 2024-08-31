package com.arbr.content_formats.format.tokenizer

import com.arbr.platform.data_structures_common.partial_order.PartialOrder

fun interface TokenizationSerializer<DocumentType, TokenType> {

    fun serializeWith(
        tokens: PartialOrder<TokenType>,
        formatter: TokenFormatter<TokenType>,
    ): DocumentType

    fun serialize(tokens: PartialOrder<TokenType>): DocumentType {
        return serializeWith(tokens, TokenFormatter.plain())
    }
}
