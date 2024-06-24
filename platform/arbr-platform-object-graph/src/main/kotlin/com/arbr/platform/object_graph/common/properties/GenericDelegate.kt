package com.arbr.og.object_model.common.properties

import kotlin.reflect.KProperty

open class GenericDelegate<V>(private val get: () -> V) {
    operator fun getValue(view: Any, property: KProperty<*>): V {
        return get()
    }

    fun writeWith(set: (V) -> Unit): GenericWritableDelegate<V> {
        return GenericWritableDelegate(get, set)
    }
}
