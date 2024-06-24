package com.arbr.platform.alignable.util

import org.slf4j.LoggerFactory
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
abstract class Dijkstra<V> {

    abstract val start: V
    abstract fun neighbors(node: V): List<Pair<V, Double>>

    fun dijkstra(matchTarget: (V) -> Boolean): Pair<Map<V, Double>, Map<V, V?>> {
        val distances = mutableMapOf<V, Double>().withDefault { Double.POSITIVE_INFINITY }
        val predecessors = mutableMapOf<V, V?>()
        val priorityQueue = PriorityQueue<Pair<V, Double>>(compareBy { it.second })

        distances[start] = 0.0
        priorityQueue.add(start to 0.0)

        while (priorityQueue.isNotEmpty()) {
            val (current, currentDistance) = priorityQueue.poll()

            if (matchTarget(current)) break
            if (currentDistance > distances.getValue(current)) continue

//            baseLogger.info("Traversing from $current")

            for ((neighbor, weight) in neighbors(current)) {
                val distanceThroughCurrent = currentDistance + weight
                if (distanceThroughCurrent < distances.getValue(neighbor)) {
                    distances[neighbor] = distanceThroughCurrent
                    predecessors[neighbor] = current
                    priorityQueue.add(neighbor to distanceThroughCurrent)

//                    baseLogger.info("  Added $neighbor")
                }
            }
        }

        return distances to predecessors
    }

    fun shortestPath(matchTarget: (V) -> Boolean): Pair<List<V>, Double>? {
        val (distances, predecessors) = dijkstra(matchTarget)

        val path = mutableListOf<V>()
        val match = predecessors.keys.firstOrNull { matchTarget(it) } ?: return null
        var current: V? = match

        if (predecessors[current] == null && current != start) {
            return null // Target is not reachable from start
        }

        while (current != null) {
            path.add(current)
            current = predecessors[current]
        }

        path.reverse()
        val distance = distances[match] ?: return null
        return path to distance
    }

    fun shortestPath(target: V): Pair<List<V>, Double>? {
        return shortestPath { it == target }
    }

    companion object {
        private val baseLogger = LoggerFactory.getLogger(Dijkstra::class.java)
    }
}
