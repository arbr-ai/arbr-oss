package com.arbr.model_suite.predictive_models.linear_tree_indent

data class LinearTreeIndentNodeInfo(
    val preIndex: Int,
    val postIndex: Int,
    val node: SegmentIndexReprTreeNode,
    val ancestors: List<Int>,
    val lineMates: List<Int>,
    val whitespaceUnitCount: Int,
    val whitespaceUnitCountChange: Int,
    val nearestNeighborPreIndexes: List<Int>,
    val prevLineTokenCodes: List<Int>,
    val prevLineIndent: Int,
    val postLineTokenCodes: List<Int>,
    val postLineIndent: Int,
)