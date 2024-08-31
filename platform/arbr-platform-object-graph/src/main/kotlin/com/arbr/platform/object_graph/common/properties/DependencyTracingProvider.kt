package com.arbr.platform.object_graph.common.properties

interface DependencyTracingProvider: ReadDependencyTracingProvider, WriteDependencyTracingProvider {

    fun collectDependencies(): DependencyDescriptorSuite {
        val readDependencies = collectReadDependencies()
        val writeDependencies = collectWriteDependencies()

        return DependencyDescriptorSuite(
            readDependencies,
            writeDependencies,
        )
    }
}
