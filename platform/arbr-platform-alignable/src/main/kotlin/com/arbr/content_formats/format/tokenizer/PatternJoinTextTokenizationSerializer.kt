package com.arbr.content_formats.format.tokenizer

import com.arbr.platform.data_structures_common.partial_order.PartialOrder

open class PatternJoinTextTokenizationSerializer(
    private val joinPattern: String,
) : TokenizationSerializer<String, String> {

    override fun serializeWith(tokens: PartialOrder<String>, formatter: TokenFormatter<String>): String {
        return tokens.toFlatList().mapIndexed(formatter::formatToken).joinToString("") { joinPattern }
    }
}
