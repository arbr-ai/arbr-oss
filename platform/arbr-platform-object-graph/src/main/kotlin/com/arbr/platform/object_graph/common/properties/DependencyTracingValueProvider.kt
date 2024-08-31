package com.arbr.platform.object_graph.common.properties

interface DependencyTracingValueProvider<ValueType> {
    fun provideValue(): ValueType

    fun <W> transformWith(
        f: (ValueType) -> W,
    ): DependencyTracingValueProvider<W>

    fun providingValue() = delegate<ValueType> {
        provideValue()
    }
}
