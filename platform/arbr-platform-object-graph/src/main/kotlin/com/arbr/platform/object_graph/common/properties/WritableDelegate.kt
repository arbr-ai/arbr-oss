package com.arbr.platform.object_graph.common.properties

fun <V> writableDelegate(
    get: () -> V,
    set: (V) -> Unit,
): GenericWritableDelegate<V> {
    return GenericWritableDelegate(get, set)
}