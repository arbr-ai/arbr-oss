package com.arbr.core_web_dev.util.file_segments

import com.arbr.platform.object_graph.core.partial.PartialFileSegmentOp
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap

/**
 * To make a homogenous DAG, we have to separate heterogenous children and treat them similar to properties.
 */
data class FileSegmentHeterogenousChildren(
    val fileSegmentOps: ImmutableLinkedMap<String, PartialFileSegmentOp>?,
)