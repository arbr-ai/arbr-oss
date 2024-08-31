package com.arbr.platform.object_graph.common.properties

import com.arbr.platform.object_graph.common.model.CompoundPropertyIdentifier

fun interface CollectionDependencyTracingValueProvider<E> {
    fun provideValue(identifier: CompoundPropertyIdentifier): E
}