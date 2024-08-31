package com.arbr.platform.object_graph.common.properties

import com.arbr.platform.object_graph.common.properties.DependencyDescriptorSet

data class DependencyDescriptorSuite(
    val readDependencySet: DependencyDescriptorSet,
    val writeDependencySet: DependencyDescriptorSet,
)