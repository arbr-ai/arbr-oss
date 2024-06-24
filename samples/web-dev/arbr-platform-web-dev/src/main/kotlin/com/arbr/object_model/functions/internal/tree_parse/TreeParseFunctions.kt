package com.arbr.object_model.functions.internal.tree_parse

import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.object_model.core.resource.field.ArbrFileContentValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph

interface TreeParseFunctions {

    fun computeSegments(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        fileUuid: String,
        filePath: ArbrFileFilePathValue,
        fileContent: ArbrFileContentValue,
    ): List<PartialFileSegment>
}
