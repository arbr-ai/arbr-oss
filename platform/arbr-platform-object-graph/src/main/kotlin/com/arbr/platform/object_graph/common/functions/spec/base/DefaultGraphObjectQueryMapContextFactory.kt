package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.og.object_model.common.properties.NonNullRequirementsProvider
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext

class DefaultGraphObjectQueryMapContextFactory(
    private val resourceFunctionContext: ResourceFunctionContext,
): GraphObjectQueryMapContextFactory {
    private fun newGraphObjectQueryMapContext(): GraphObjectQueryMapContext {
        return GraphObjectQueryMapContextImpl(
            BaseResourceHelperFunctionInputSpecContext(),
            resourceFunctionContext.nonNullRequirementsProvider,
        )
    }

    override fun newContext(): GraphObjectQueryMapContext {
        return newGraphObjectQueryMapContext()
    }

    private class GraphObjectQueryMapContextImpl(
        resourceHelperFunctionInputSpecContext: ResourceHelperFunctionInputSpecContext,
        nonNullRequirementsProvider: NonNullRequirementsProvider,
    ): GraphObjectQueryMapContext,
        ResourceHelperFunctionInputSpecContext by resourceHelperFunctionInputSpecContext,
        NonNullRequirementsProvider by nonNullRequirementsProvider
}