package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext

interface ResourceConfigurableTarget<Artifact: Any> {
    fun configure(
        context: ResourceFunctionContext,
    ): Artifact
}