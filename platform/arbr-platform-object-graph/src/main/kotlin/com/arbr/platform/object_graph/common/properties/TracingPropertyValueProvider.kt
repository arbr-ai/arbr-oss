package com.arbr.platform.object_graph.common.properties

interface TracingPropertyValueProvider {
    fun <T> provideValue(): T

    fun <T> provideCollection(): Collection<T> {
        TODO()
    }

    fun <T> providingValue() = delegate<T> {
        provideValue()
    }

    fun <T> providingCollection() = delegate<Collection<T>> {
        provideCollection()
    }
}