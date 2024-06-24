package com.arbr.platform.object_graph.common

interface ForeignKeyChildListResult<Key : Any, Value : Any> {
    val parentKey: Key
    val isAccepted: Boolean
    val children: List<Value>
}