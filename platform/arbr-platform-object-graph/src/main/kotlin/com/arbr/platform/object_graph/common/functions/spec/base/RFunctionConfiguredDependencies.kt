package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.properties.DependencyDescriptorSuite

data class RFunctionConfiguredDependencies<RV : ResourceView<*>>(
    val resourceFunction: RValueFunction<RV, Unit>,
    val dependencyDescriptorSuite: DependencyDescriptorSuite,
)