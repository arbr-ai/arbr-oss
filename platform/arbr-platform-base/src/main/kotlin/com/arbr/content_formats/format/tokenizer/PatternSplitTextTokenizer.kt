package com.arbr.content_formats.format.tokenizer

import com.arbr.data_structures_common.partial_order.LinearOrderList

/**
 * To use a regex split without losing information would require something like a graph morphism so just use a string
 * for now
 * TODO: Note tokens might need to be distinguishable!
 */
open class PatternSplitTextTokenizer(
    val splitPattern: String,
): IndexedTextTokenizer {

    override fun tokenize(document: String): LinearOrderList<Pair<Int, String>> {
        return LinearOrderList(document.split(splitPattern).withIndex().map { it.index to it.value })
    }
}

open class RegexPatternSplitTextTokenizer(
    val splitPattern: Regex,
): IndexedTextTokenizer {

    override fun tokenize(document: String): LinearOrderList<Pair<Int, String>> {
        return LinearOrderList(document.split(splitPattern).withIndex().map { it.index to it.value })
    }
}