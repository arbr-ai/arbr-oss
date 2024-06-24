package com.arbr.og.object_model.common.functions.spec.base

import kotlin.reflect.KProperty

open class Delegate<V>(private val get: () -> V) {
    operator fun getValue(view: Any?, property: KProperty<*>): V {
        return get()
    }
}