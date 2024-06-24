package com.arbr.content_formats.format

import com.arbr.content_formats.format.tokenizer.LineTokenizer
import com.arbr.content_formats.format.tokenizer.Tokenizer
import com.arbr.data_structures_common.partial_order.LinearOrderList

class DiffLiteralSourceDocumentTokenizer :
    Tokenizer<DiffLiteralSourceDocument, LinearOrderList<DiffOperation>, DiffOperation> {
    private val docTokenizer = LineTokenizer().mapInto(LinearOrderList(emptyList())) { (i, line) ->
        DiffOperation(DiffOperationKind.NOP, line, i)
    }

    override fun tokenize(document: DiffLiteralSourceDocument): LinearOrderList<DiffOperation> {
        return docTokenizer.tokenize(document.text)
    }
}
