package com.arbr.ml.search

import java.util.*

class ConcurrentPriorityQueue<T>(
    private val comparator: Comparator<T>
) {
    private val priorityQueue = PriorityQueue(comparator)

    @Synchronized
    fun add(e: T): Boolean {
        return priorityQueue.add(e)
    }

    @Synchronized
    fun isEmpty(): Boolean {
        return priorityQueue.isEmpty()
    }

    @Synchronized
    fun isNotEmpty(): Boolean {
        return priorityQueue.isNotEmpty()
    }

    @Synchronized
    fun getSize(): Int {
        return priorityQueue.size
    }

    @Synchronized
    fun poll(): T {
        return priorityQueue.poll()
    }

}