package com.arbr.og.object_model.common.properties

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
