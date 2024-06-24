package com.arbr.og.object_model.common.values

data class SourcedValueGeneratorInfo(
    /**
     * Generators, aka inbound edges in the object pipeline graph
     */
    val generators: List<SourcedValueGeneratorInboundEdge>
)