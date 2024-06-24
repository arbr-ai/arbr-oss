package com.arbr.core_web_dev.util.file_segments

import com.arbr.object_model.core.resource.field.*

/**
 * Breakup of a file segment partial into components to divorce nodes from children
 * This addresses numerous issues with keeping nodes + children in sync
 * Eventually should move this into the Partials lib but better to test the most important use case first
 */
data class FileSegmentProperties(
    val uuid: String,

    val contentType: ArbrFileSegmentContentTypeValue? = null,
    val ruleName: ArbrFileSegmentRuleNameValue? = null,
    val name: ArbrFileSegmentNameValue? = null,
    val elementIndex: ArbrFileSegmentElementIndexValue? = null,
    val startIndex: ArbrFileSegmentStartIndexValue? = null,
    val endIndex: ArbrFileSegmentEndIndexValue? = null,
    val summary: ArbrFileSegmentSummaryValue? = null,
    val containsTodo: ArbrFileSegmentContainsTodoValue? = null,
)