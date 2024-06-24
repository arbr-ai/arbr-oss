package com.arbr.types.homotopy

import com.arbr.util_common.LexIntSequence

data class OrderedPlainTypeNode(
    val lexIntSequence: LexIntSequence,
    val node: PlainTypeNode,
)