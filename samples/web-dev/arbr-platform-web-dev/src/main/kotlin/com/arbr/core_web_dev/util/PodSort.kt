package com.arbr.core_web_dev.util

import kotlin.math.abs
import kotlin.math.sqrt

object PodSort {

    private fun innerPodSort(
        dependencyMap: Map<String, List<String>>,
        indexMap: Map<String, Int>,
        path: List<String>,
        hash: Int,
        baseCost: Double,
        memo: MutableMap<Pair<Int, String?>, Pair<Double, List<String>>>
    ): Pair<Double, List<String>> {
        val frontier = dependencyMap.filter { (k, deps) ->
            k !in path && deps.all { it in path }
        }.keys.toMutableSet()

        val memoKey = hash to path.lastOrNull()
        if (memoKey in memo) {
            return memo[memoKey]!!
        }

        if (frontier.isEmpty()) {
            memo[memoKey] = baseCost to path
            return baseCost to path
        }

        // Greedy criterion - glob any frontier elements that are consecutive with the path
        if (path.isNotEmpty()) {
            val lastElt = path.last()
            val finalIndex = indexMap[lastElt]!!

            val nextElt = frontier.firstOrNull {
                indexMap[it] == finalIndex + 1
            }
            if (nextElt != null) {
                val nextPath = path + nextElt
                val nextHash = hash.xor(nextElt.hashCode())

                val (cost, innerPath) = innerPodSort(dependencyMap, indexMap, nextPath, nextHash, baseCost + 1.0, memo)

                memo[memoKey] = cost to innerPath
                return cost to innerPath
            }
        }

        val lengths = frontier.associateWith {
            if (path.isEmpty()) {
                0.0
            } else {
                val finalIndex = indexMap[path.last()]!!
                val nextIndex = indexMap[it]!!

                // Give slight preference to ascending indices
                val signCoefficient = if (nextIndex >= finalIndex) {
                    1.0
                } else {
                    1.5
                }

                signCoefficient * sqrt(abs(finalIndex - nextIndex).toDouble())
            }
        }

        var minCost: Double? = null
        var bestPath: List<String>? = null
        for (f in frontier) {
            val nextPath = path + f
            val nextHash = hash.xor(f.hashCode())
            val thisCost = lengths[f]!!

            val (cost, innerPath) = innerPodSort(dependencyMap, indexMap, nextPath, nextHash, baseCost + thisCost, memo)

            if (minCost == null || cost < minCost) {
                minCost = cost
                bestPath = innerPath
            }
        }

        val result = minCost!! to bestPath!!
        memo[memoKey] = result
        return result
    }

    /**
     * Sort a list of identifiers so that the resulting list is in topological order wrt the given dependency map, but
     * minimize the number of movements to preserve as much of the original ordering as possible.
     * Application: reorder file seg ops to respect dependencies while maintaining locality of relevant information.
     *
     * Note: Doesn't attempt to identify unsolvable cases
     */
    fun podSort(
        originalList: List<String>,
        dependencyMap: Map<String, List<String>>,
    ): List<String> {
        val indexMap = originalList.withIndex().associate { (i, k) -> k to i }

        val memo = mutableMapOf<Pair<Int, String?>, Pair<Double, List<String>>>()
        val (_, path) = innerPodSort(
            dependencyMap,
            indexMap,
            emptyList(),
            0,
            0.0,
            memo
        )

        return path
    }

}