package com.arbr.platform.object_graph.common.properties

import kotlin.reflect.KProperty

class GenericWritableDelegate<V>(
    private val getInner: () -> V,
    private val setInner: (V) -> Unit,
): GenericDelegate<V>(getInner) {
    operator fun setValue(view: Any, property: KProperty<*>, value: V) {
        setInner(value)
    }
}
