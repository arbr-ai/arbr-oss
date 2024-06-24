package com.arbr.platform.object_graph.util

import java.util.*

class OrderedMapCollector<K, V> private constructor(
    private val treeMap: TreeMap<K, V>,
): MutableMap<K, V>, Map<K, V> by treeMap {

    constructor(
        ordinal: (K) -> Int,
    ): this(TreeMap<K, V>(compareBy(ordinal)))

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = treeMap.entries

    override val keys: MutableSet<K>
        get() = treeMap.keys

    override val values: MutableCollection<V>
        get() = treeMap.values

    fun result(): Collection<V> {
        return treeMap.values
    }

    override fun putAll(from: Map<out K, V>) {
        treeMap.putAll(from)
    }

    override fun put(key: K, value: V): V? {
        treeMap[key] = value
        return null
    }

    override fun clear() {
        treeMap.clear()
    }

    override fun remove(key: K): V? {
        return treeMap.remove(key)
    }
}