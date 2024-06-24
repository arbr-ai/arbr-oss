package com.arbr.content_formats.format.tokenizer

import com.arbr.platform.data_structures_common.partial_order.LinearOrderList

class LineTokenizer : IndexedTextTokenizer {

    private val innerTokenizer = PatternSplitTextTokenizer("\n")
        .mapIntoNotNull(LinearOrderList(emptyList())) {
//            if (it.second.isEmpty()) {
//                null
//            } else {
//                it
//            }

            it
        }

    override fun tokenize(document: String): LinearOrderList<Pair<Int, String>> {
        return innerTokenizer.tokenize(document)
    }
}
