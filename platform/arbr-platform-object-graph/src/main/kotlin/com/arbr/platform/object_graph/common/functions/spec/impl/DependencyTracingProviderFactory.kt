package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.og.object_model.common.properties.DependencyTracingProvider

interface DependencyTracingProviderFactory {

    fun newTracingProvider(): DependencyTracingProvider
}