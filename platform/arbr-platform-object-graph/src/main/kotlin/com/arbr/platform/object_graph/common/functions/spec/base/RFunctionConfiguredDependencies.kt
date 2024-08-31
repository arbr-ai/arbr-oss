package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.common.properties.DependencyDescriptorSuite

data class RFunctionConfiguredDependencies<RV : ResourceView<*>>(
    val resourceFunction: RValueFunction<RV, Unit>,
    val dependencyDescriptorSuite: DependencyDescriptorSuite,
)