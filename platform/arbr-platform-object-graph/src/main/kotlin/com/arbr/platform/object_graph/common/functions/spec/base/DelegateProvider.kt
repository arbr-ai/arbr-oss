package com.arbr.platform.object_graph.common.functions.spec.base

import kotlin.reflect.KProperty

interface DelegateProvider<T> {
    fun getOrConfigure(): T

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T>
}