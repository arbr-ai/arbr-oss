package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier

fun interface CollectionDependencyTracingValueProvider<E> {
    fun provideValue(identifier: CompoundPropertyIdentifier): E
}