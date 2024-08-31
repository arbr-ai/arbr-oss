package com.arbr.object_model.functions.internal.tree_parse

import com.arbr.platform.object_graph.core.partial.PartialFileSegment
import com.arbr.platform.object_graph.core.resource.field.ArbrFileContentValue
import com.arbr.platform.object_graph.core.resource.field.ArbrFileFilePathValue
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.types.ArbrForeignKey
import org.springframework.stereotype.Component

@Component
class TreeParseFunctionsImpl: TreeParseFunctions {
    override fun computeSegments(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        fileUuid: String,
        filePath: ArbrFileFilePathValue,
        fileContent: ArbrFileContentValue
    ): List<PartialFileSegment> {
        TODO("Not yet implemented")
    }
}
