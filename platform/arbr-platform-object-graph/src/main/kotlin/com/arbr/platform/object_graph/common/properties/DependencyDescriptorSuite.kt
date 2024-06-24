package com.arbr.og.object_model.common.properties

data class DependencyDescriptorSuite(
    val readDependencySet: DependencyDescriptorSet,
    val writeDependencySet: DependencyDescriptorSet,
)