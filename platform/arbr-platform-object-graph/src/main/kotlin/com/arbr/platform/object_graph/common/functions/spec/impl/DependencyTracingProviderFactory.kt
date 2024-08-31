package com.arbr.platform.object_graph.common.functions.spec.impl

import com.arbr.platform.object_graph.common.properties.DependencyTracingProvider

interface DependencyTracingProviderFactory {

    fun newTracingProvider(): DependencyTracingProvider
}