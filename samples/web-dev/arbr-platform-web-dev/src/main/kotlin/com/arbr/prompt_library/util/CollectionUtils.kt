package com.arbr.prompt_library.util

import java.util.*

inline fun <T> Iterable<T>.splitOn(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    val arrayListSatisfied = ArrayList<T>()
    val arrayListUnsatisfied = ArrayList<T>()

    for (element in this) if (predicate(element)) arrayListSatisfied.add(element) else arrayListUnsatisfied.add(element)

    return arrayListSatisfied to arrayListUnsatisfied
}

/**
 * Collate elements by sequential "runs" of buckets defined by the mapping.
 */
fun <T, U> List<T>.collateBy(key: (T) -> U): List<Pair<U, List<T>>> {
    if (isEmpty()) {
        return emptyList()
    }

    val collated = mutableListOf<Pair<U, List<T>>>()

    val firstElt = first()
    var bucket = mutableListOf(firstElt)
    var inBucket = key(firstElt)
    for (elt in drop(1)) {
        val eltInBucket = key(elt)
        if (eltInBucket == inBucket) {
            bucket.add(elt)
        } else {
            collated.add(inBucket to bucket)

            bucket = mutableListOf(elt)
            inBucket = eltInBucket
        }
    }

    // Last bucket always nonempty
    collated.add(inBucket to bucket)

    return collated
}

fun <T, R : Comparable<R>> Iterable<T>.minKBy(k: Int, selector: (T) -> R): List<T> {
    val pq = PriorityQueue<Pair<T, R>>(compareBy { (_, r) ->
        r
    })

    val iterator = iterator()
    if (!iterator.hasNext()) return emptyList()

    do {
        val t = iterator.next()
        val r = selector(t)
        pq.add(t to r)
    } while (iterator.hasNext())

    return List(k) {
        pq.poll().first
    }
}