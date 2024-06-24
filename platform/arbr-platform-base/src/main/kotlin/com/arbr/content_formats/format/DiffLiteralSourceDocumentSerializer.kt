package com.arbr.content_formats.format

import com.arbr.content_formats.format.tokenizer.TokenFormatter
import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import com.arbr.data_structures_common.partial_order.PartialOrder
import com.arbr.data_structures_common.partial_order.PartialOrderFlatteningScheme

class DiffLiteralSourceDocumentSerializer :
    TokenizationSerializer<DiffLiteralSourceDocument, DiffOperation> {

    override fun serializeWith(
        tokens: PartialOrder<DiffOperation>,
        formatter: TokenFormatter<DiffOperation>
    ): DiffLiteralSourceDocument {
        val tokenList = tokens.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST)

        val contentStringBuilder = StringBuilder().run {
            var lineIndex = 0
            for (token in tokenList) {
                // All operations other than DEL indicate the line content should land in the resulting document
                val formattedToken = formatter.formatToken(lineIndex, token)
                when (formattedToken.kind) {
                    DiffOperationKind.NUL,
                    DiffOperationKind.ADD,
                    DiffOperationKind.NOP -> append((if (lineIndex == 0) "" else "\n") + formattedToken.lineContent)
                        .also { lineIndex++ }

                    DiffOperationKind.DEL -> {
                        // Do nothing
                    }
                }
            }
            toString()
        }

        return DiffLiteralSourceDocument(contentStringBuilder)
    }
}
