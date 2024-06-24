package com.arbr.og.object_model.common.properties

import kotlin.reflect.KProperty

class GenericWritableDelegate<V>(
    private val getInner: () -> V,
    private val setInner: (V) -> Unit,
): GenericDelegate<V>(getInner) {
    operator fun setValue(view: Any, property: KProperty<*>, value: V) {
        setInner(value)
    }
}
