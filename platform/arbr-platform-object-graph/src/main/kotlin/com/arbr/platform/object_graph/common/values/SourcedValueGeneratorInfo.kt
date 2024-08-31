package com.arbr.platform.object_graph.common.values

data class SourcedValueGeneratorInfo(
    /**
     * Generators, aka inbound edges in the object pipeline graph
     */
    val generators: List<SourcedValueGeneratorInboundEdge>
)