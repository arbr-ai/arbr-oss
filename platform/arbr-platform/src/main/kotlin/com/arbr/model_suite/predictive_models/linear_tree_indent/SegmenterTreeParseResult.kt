package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.topdown.parsers.common.model.SyntaxErrorModel

data class SegmenterTreeParseResult(
    val segmentReprTree: SegmentIndexReprTree,
    val syntaxErrors: List<SyntaxErrorModel>,
    val success: Boolean,
)