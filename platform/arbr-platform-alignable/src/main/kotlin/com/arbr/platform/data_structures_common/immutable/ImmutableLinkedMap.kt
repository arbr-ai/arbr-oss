package com.arbr.platform.data_structures_common.immutable

fun <K, V> immutableLinkedMapOf(
    vararg pairs: Pair<K, V>,
) = ImmutableLinkedMap(*pairs)

private data class MapEntry<K, V>(
    override val key: K,
    override val value: V
) : Map.Entry<K, V>

class ImmutableLinkedMap<K, V>(
    pairList: List<Pair<K, V>>,
) : Map<K, V> {
    private val keyIndexMap: Map<K, Int>
    private val valueList: List<V>

    init {
        val kiMap = mutableMapOf<K, Int>()
        val vList = mutableListOf<V>()

        var i = 0
        for ((k, v) in pairList) {
            if (k in kiMap) {
                continue
            }

            kiMap[k] = i
            vList.add(v)

            i++
        }

        keyIndexMap = kiMap
        valueList = vList
    }

    constructor(
        vararg pairs: Pair<K, V>,
    ): this(pairs.toList())

    override val entries: Set<Map.Entry<K, V>>
        get() = keyIndexMap.map { (k, i) ->
            MapEntry(k, valueList[i])
        }.toSet()

    override val keys: Set<K>
        get() = keyIndexMap.keys

    override val size: Int
        get() = keyIndexMap.size

    override val values: Collection<V>
        get() = valueList

    override fun isEmpty(): Boolean {
        return keyIndexMap.isEmpty()
    }

    override fun get(key: K): V? {
        return keyIndexMap[key]?.let { valueList[it] }
    }

    override fun containsValue(value: V): Boolean {
        return valueList.contains(value)
    }

    override fun containsKey(key: K): Boolean {
        return keyIndexMap.containsKey(key)
    }

    fun <R> mapValues(transform: (Map.Entry<K, V>) -> R): ImmutableLinkedMap<K, R> {
        return ImmutableLinkedMap(this.map { it.key to transform(it) })
    }

    fun <R: Any> mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?): ImmutableLinkedMap<K, R> {
        return ImmutableLinkedMap(
            this.mapNotNull { entry ->
                val (key, value) = entry
                transform(entry)?.let {
                    key to it
                }
            }
        )
    }

    fun adding(key: K, value: V): ImmutableLinkedMap<K, V> {
        val index = keyIndexMap[key]
        val numEntries = valueList.size
        val newPairList = if (index == null) {
            val newPairs = arrayOfNulls<Pair<K, V>>(numEntries + 1)
            for ((k, i) in keyIndexMap) {
                newPairs[i] = k to valueList[i]
            }
            newPairs[numEntries] = key to value
            newPairs
        } else {
            val newPairs = arrayOfNulls<Pair<K, V>>(numEntries)
            for ((k, i) in keyIndexMap) {
                newPairs[i] = k to valueList[i]
            }
            newPairs[index] = key to value
            newPairs
        }.filterNotNull()

        return ImmutableLinkedMap(newPairList)
    }

    fun updatingFromPairs(otherPairs: Collection<Pair<K, V>>): ImmutableLinkedMap<K, V> {
        val newPairList = arrayOfNulls<Pair<K, V>>(valueList.size).let { arr ->
            for ((k, i) in keyIndexMap) {
                arr[i] = k to valueList[i]
            }
            arr.filterNotNull().toMutableList()
        }

        val indexMap = keyIndexMap.toMutableMap()

        for (p in otherPairs) {
            val k = p.first
            val i = indexMap[k]
            if (i == null) {
                val nextI = newPairList.size
                newPairList.add(p)
                indexMap[k] = nextI
            } else {
                newPairList[i] = p
            }
        }

        return ImmutableLinkedMap(
            newPairList
        )
    }

    fun updatingFrom(otherEntries: Collection<Map.Entry<K, V>>): ImmutableLinkedMap<K, V> {
        return updatingFromPairs(otherEntries.map { it.key to it.value })
    }

    fun updatingFrom(otherMap: Map<K, V>): ImmutableLinkedMap<K, V> {
        return updatingFrom(otherMap.entries)
    }

    override fun toString(): String {
        return "{${keyIndexMap.entries.joinToString(", ") { "${it.key}=${valueList[it.value]}" }}}"
    }

    companion object {
        fun <K, V> ofLinkedHashMap(linkedHashMap: LinkedHashMap<K, V>): ImmutableLinkedMap<K, V> {
            return ImmutableLinkedMap(
                linkedHashMap.entries.map { it.key to it.value }
            )
        }
    }

}