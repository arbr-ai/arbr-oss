package com.arbr.types.homotopy

import com.arbr.platform.object_graph.util.LexIntSequence

data class OrderedPlainTypeNode(
    val lexIntSequence: LexIntSequence,
    val node: PlainTypeNode,
)