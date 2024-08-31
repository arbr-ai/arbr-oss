package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.common.functions.platform.ResourceFunctionContext

interface ResourceConfigurableTarget<Artifact: Any> {
    fun configure(
        context: ResourceFunctionContext,
    ): Artifact
}