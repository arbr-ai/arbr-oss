package com.arbr.content_formats.format

import com.arbr.content_formats.format.tokenizer.LineTokenizer
import com.arbr.content_formats.format.tokenizer.Tokenizer
import com.arbr.platform.data_structures_common.partial_order.LinearOrderList

/**
 * Eventually this should be a lenient parser
 */
class DiffLiteralPatchTokenizer :
    Tokenizer<DiffLiteralPatch, LinearOrderList<DiffOperation>, DiffOperation> {
    private val parser = DiffOperationParser()

    private val diffTokenizer = LineTokenizer().mapIntoNotNull(LinearOrderList(emptyList())) { (lineNumber, line) ->
        parser
            .parse(lineNumber, line)
            .leftOrNull()
    }

    override fun tokenize(document: DiffLiteralPatch): LinearOrderList<DiffOperation> {
        return diffTokenizer.tokenize(document.text)
    }

}
