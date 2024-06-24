package com.arbr.og.object_model.common.functions.spec.base

import kotlin.reflect.KProperty

interface DelegateProvider<T> {
    fun getOrConfigure(): T

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T>
}