package com.arbr.core_web_dev.util.file_segments

import com.arbr.core_web_dev.util.PodSort
import com.arbr.util_common.invariants.Invariants
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

object FileSegmentOperationDependencyUtils {

    private val logger = LoggerFactory.getLogger(FileSegmentOperationDependencyUtils::class.java)

    /**
     * Add entries for all nodes that appear in the graph but not the keys of the dependency map with empty dependencies.
     */
    fun fillCompleteGraph(
        dependencyMap: Map<String, List<String>>,
    ): Map<String, List<String>> {
        val nodes = (dependencyMap.keys + dependencyMap.values.flatten().toSet()).toList().sorted()

        val mutableMap = dependencyMap.toMutableMap()

        for (node in nodes) {
            if (node !in dependencyMap.keys) {
                mutableMap[node] = emptyList()
            }
        }

        return mutableMap
    }

    fun connectedComponents(
        dependencyMap: Map<String, List<String>>,
    ): List<Map<String, List<String>>> {
        val nodes = (dependencyMap.keys + dependencyMap.values.flatten().toSet()).toList().sorted()

        val components = nodes.map { mutableListOf<String>(it) }
        val componentPointers = linkedMapOf<String, AtomicInteger>()

        nodes.forEachIndexed { index, k ->
            componentPointers[k] = AtomicInteger(index)
        }

        var updated = true

        while (updated) {
            updated = false
            val seenComponentIndicesThisRound = mutableSetOf<Int>()

            for ((_, componentIndex) in componentPointers) {
                val componentIndexValue = componentIndex.get()
                if (componentIndexValue in seenComponentIndicesThisRound) {
                    continue
                }
                seenComponentIndicesThisRound.add(componentIndexValue)

                val component = components[componentIndex.get()]
                val frontier = component.flatMap { dependencyMap[it] ?: emptyList() }.filter { it !in component }
                val frontierComponentElements =
                    frontier.flatMap { v -> components[componentPointers[v]!!.get()] }.filter { it !in component }

                frontier.forEach {
                    componentPointers[it] = componentIndex
                }

                if (frontierComponentElements.isNotEmpty()) {
                    updated = true
                }

                component.addAll(frontierComponentElements)
            }
        }

        val finalComponents = nodes.map { componentPointers[it]!!.get() }.distinct()
            .map { i ->
                components[i].associateWith { dependencyMap[it]!! }
            }

        Invariants.check { require ->
            dependencyMap.forEach { (k, deps) ->
                val matchingComponent = finalComponents.firstOrNull { componentMap ->
                    k in componentMap.keys
                }
                require(matchingComponent != null)

                // Dependencies should all be in the same component
                deps.forEach { dep ->
                    dep in matchingComponent!!.keys
                }
            }
        }

        return finalComponents
    }

    fun getCycles(
        edgeMap: Map<String, List<String>>,
    ): List<Set<String>> {
        val reachable = mutableMapOf<String, Set<String>>()

        fun getReachableBfs(origin: String): Set<String> {
            val seen = mutableSetOf(origin)
            var frontier = setOf(origin)
            while (frontier.isNotEmpty()) {
                val newFrontier = frontier.flatMap { s ->
                    edgeMap[s] ?: emptyList()
                }.toSet() - seen

                seen.addAll(newFrontier)
                frontier = newFrontier
            }

            return seen - origin
        }

        fun getReachable(origin: String): Set<String> {
            if (origin in reachable) {
                return reachable[origin]!!
            }

            val result = getReachableBfs(origin)
            reachable[origin] = result
            return result
        }

        val allNodesOrdered = (edgeMap.keys + edgeMap.values.flatten()).distinct().sorted()
        val reachableNodeMap = allNodesOrdered.associateWith {
            getReachable(it)
        }

        // Establish cycles by equivalence relation representative
        val finalCycles = mutableListOf<Set<String>>()
        val remainingNodes = allNodesOrdered.toMutableSet()
        while (remainingNodes.isNotEmpty()) {
            val node = remainingNodes.first()
            val reachableNodes = reachableNodeMap[node]!!
            val members = reachableNodes.filter {
                node in reachableNodeMap[it]!!
            }.toSet()

            finalCycles.add(members + node)

            remainingNodes.remove(node)
            remainingNodes.removeAll(members)
        }

        // Filter length-1 classes which aren't real cycles
        // Unless a node is genuinely in its own dependency list
        return finalCycles.filter {
            it.size > 1
                    || (it.size == 1 && edgeMap[it.first()]!!.contains(it.first()))
        }
    }

    private fun breakCyclesForConnectedComponent(
        component: Map<String, List<String>>,
    ): Map<String, List<String>> {
        if (component.isEmpty()) {
            return component
        }

        val orderedKeys = component.keys.sorted()

        // Roots are just ops with no dependencies
        // TODO: Consider Q-shaped graph
        val roots = orderedKeys
            .filter { (component[it] ?: emptyList()).isEmpty() }
            .ifEmpty {
                logger.warn("No roots in file segment op dependency graph component! Using cycle representatives")
                getCycles(component).map { it.min() }
            }

        // Invert edges
        // Parent op -> "unlocked" child ops
        val invertedDependencyMap = component
            .flatMap { it.value.map { dep -> dep to it.key } }
            .groupBy { it.first }
            .mapValues { pairs -> pairs.value.map { it.second } }

        val edgesToRemove = mutableSetOf<Pair<String, String>>()

        var paths = roots.map {
            listOf(it)
        }

        while (true) {
            val nextPaths = mutableListOf<List<String>>()

            for (path in paths) {
                val tail = path.last()

                val children = invertedDependencyMap[tail] ?: emptyList()

                nextPaths.addAll(
                    children.mapNotNull { child ->
                        if (child in path) {
                            // Cycle creator
                            // Swap back to regular order
                            edgesToRemove.add(child to tail)

                            null
                        } else {
                            path + listOf(child)
                        }
                    }
                )
            }

            if (nextPaths.isEmpty()) {
                break
            }

            paths = nextPaths
        }

        return component.mapValues { (k, deps) ->
            deps.filter { v ->
                (k to v) !in edgesToRemove
            }
        }
    }

    fun breakCycles(
        dependencyMap: Map<String, List<String>>,
    ): Map<String, List<String>> {
        if (dependencyMap.isEmpty()) {
            return dependencyMap
        }

        val components = connectedComponents(dependencyMap)

        val newMap = mutableMapOf<String, List<String>>()
        components.forEach { component ->
            newMap.putAll(breakCyclesForConnectedComponent(component))
        }

        return newMap
    }

    private fun orderedTopSort(
        orderedNodeIds: List<String>,
        dependencyMap: Map<String, List<String>>,
    ): List<String> {
        return PodSort.podSort(
            orderedNodeIds,
            dependencyMap,
        )
    }

    private fun completeOrderedNodeIds(
        orderedNodeIds: List<String>,
        dependencyMapping: Map<String, List<String>>,
    ): List<String> {
        val newIds = (dependencyMapping.keys + dependencyMapping.values.flatten()).distinct()
            .filter { it !in orderedNodeIds }

        return orderedNodeIds + newIds
    }

    /**
     * Given ordered op IDs and a map of op_id -> {dependency_op_id_0, ...} with intermediary op IDs like 1-0
     * Pre-process for solvability etc.
     * Sort topologically and return an ordered list of segment op objects as containers
     * TODO: Remove transitive dependencies
     */
    fun processDependencyMapping(
        orderedNodeIds: List<String>,
        dependencyMapping: Map<String, List<String>>
    ): Pair<List<String>, Map<String, List<String>>> {
        return fillCompleteGraph(dependencyMapping)
            .let(this::breakCycles)
            .let { newDepMap ->
                orderedTopSort(completeOrderedNodeIds(orderedNodeIds, newDepMap), newDepMap) to newDepMap
            }
    }

}