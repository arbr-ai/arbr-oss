package com.arbr.core_web_dev.util.file_segments

import com.arbr.platform.object_graph.core.partial.PartialFile
import com.arbr.platform.object_graph.core.partial.PartialFileSegment
import com.arbr.platform.object_graph.core.resource.ArbrFile
import com.arbr.platform.object_graph.core.resource.ArbrFileSegment
import com.arbr.platform.object_graph.impl.PartialRef

data class FileSegmentParents(
    val file: PartialRef<out ArbrFile, PartialFile>? = null,
    val parentSegment: PartialRef<out ArbrFileSegment, PartialFileSegment>? = null,
)