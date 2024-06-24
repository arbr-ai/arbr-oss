package com.arbr.platform.object_graph.concurrency

class LockedResourceRenderException(
    val resourceUuid: String,
    val resourceDisplayName: String,
) : Exception("Currently unable to render ${resourceDisplayName}:$resourceUuid")