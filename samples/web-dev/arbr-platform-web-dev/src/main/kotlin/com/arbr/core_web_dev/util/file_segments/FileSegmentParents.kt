package com.arbr.core_web_dev.util.file_segments

import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.platform.object_graph.impl.PartialRef

data class FileSegmentParents(
    val file: PartialRef<out ArbrFile, PartialFile>? = null,
    val parentSegment: PartialRef<out ArbrFileSegment, PartialFileSegment>? = null,
)