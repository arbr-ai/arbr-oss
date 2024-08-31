package com.arbr.platform.object_graph.common.properties

fun <V> delegate(f: () -> V): GenericDelegate<V> {
    return GenericDelegate(f)
}
