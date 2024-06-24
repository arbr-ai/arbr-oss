package com.arbr.og.object_model.common.properties

interface DependencyTracingValueProvider<ValueType> {
    fun provideValue(): ValueType

    fun <W> transformWith(
        f: (ValueType) -> W,
    ): DependencyTracingValueProvider<W>

    fun providingValue() = delegate<ValueType> {
        provideValue()
    }
}
