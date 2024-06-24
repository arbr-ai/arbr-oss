package com.arbr.graphql.converter.util

object ListUtils

/**
 * This could be made faster by using indices as a proxy to avoid hashing big objects
 */
fun <T> List<T>.topSortWith(
    comparator: Comparator<T>,
    dependencies: (T) -> List<T>,
): List<T> {
    // Create a map of each element to its unsatisfied dependencies
    val unsatisfiedDependencies = mutableMapOf<T, MutableSet<T>>()
    forEach { element ->
        unsatisfiedDependencies[element] = dependencies(element).toMutableSet()
    }
    val visited = mutableSetOf<T>()

    // Create a map to store the layer index of each element
    val layerIndex = mutableMapOf<T, Int>()

    // Perform a breadth-first search to identify the layer index of each element
    val frontier = mutableListOf<T>()
    forEach { element ->
        if (unsatisfiedDependencies[element]?.isEmpty() == true) {
            frontier.add(element)
            layerIndex[element] = 0
        }
    }
    visited.addAll(frontier)

    var currentLayer = 0
    while (frontier.isNotEmpty()) {
        val nextFrontier = mutableSetOf<T>()
        frontier.forEach { element ->
            unsatisfiedDependencies.forEach { (dependent, dependencies) ->
                if (dependent !in visited) {
                    dependencies.remove(element)
                    if (dependencies.isEmpty()) {
                        nextFrontier.add(dependent)
                        layerIndex[dependent] = currentLayer + 1
                    }
                }
            }
        }
        frontier.clear()
        frontier.addAll(nextFrontier)
        visited.addAll(nextFrontier)
        currentLayer++
    }

    // Sort elements by their layer index and break ties with lexicographic comparison
    return sortedWith(compareBy<T> { layerIndex[it] ?: Int.MAX_VALUE }.then(comparator))
}

fun <T: Comparable<T>> List<T>.topSort(
    dependencies: (T) -> List<T>,
): List<T> {
    return topSortWith(
        { t0, t1 -> t0.compareTo(t1) },
        dependencies,
    )
}
