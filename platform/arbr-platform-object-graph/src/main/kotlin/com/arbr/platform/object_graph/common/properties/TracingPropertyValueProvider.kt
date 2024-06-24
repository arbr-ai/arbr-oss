package com.arbr.og.object_model.common.properties

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