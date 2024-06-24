package com.arbr.og.object_model.common.properties

fun <V> writableDelegate(
    get: () -> V,
    set: (V) -> Unit,
): GenericWritableDelegate<V> {
    return GenericWritableDelegate(get, set)
}