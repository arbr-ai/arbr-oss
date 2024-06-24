package com.arbr.og.object_model.common.properties

fun <V> delegate(f: () -> V): GenericDelegate<V> {
    return GenericDelegate(f)
}
