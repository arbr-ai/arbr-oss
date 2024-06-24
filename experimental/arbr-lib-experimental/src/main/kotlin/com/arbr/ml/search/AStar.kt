package com.arbr.ml.search

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

object AStar {
    private val logger = LoggerFactory.getLogger(AStar::class.java)

    private fun <T> reconstructPath(cameFrom: Map<T, T>, start: T, current: T): List<T> {
        val path = mutableListOf(current)
        var currentInPath = current
        while (cameFrom.containsKey(currentInPath)) {
            currentInPath = cameFrom[currentInPath]!!
            path.add(currentInPath)
        }
        path.add(start)
        return path.reversed()
    }

    fun <T> aStarSearch(
        aStarGraph: AStarGraph<T>,
        lossThreshold: Double,
        maxNumEvaluations: Int = 10_000,
        logInfo: Boolean = true,
        parallelism: Int = 8,
    ): Pair<List<T>?, Double?> {
        fun log(s: String) {
            if (logInfo) {
                logger.info(s)
            } else {
                logger.debug(s)
            }
        }
        log("Starting")

        val closedSet = ConcurrentHashMap<T, Unit>()
        val openSet = ConcurrentPriorityQueue(compareBy<Pair<Double, T>> { it.first })
        openSet.add(0.0 + aStarGraph.heuristic(aStarGraph.start) to aStarGraph.start)
        val neighborCounter = ConcurrentHashMap<T, Int>()
        neighborCounter[aStarGraph.start] = 1

        val gScore = ConcurrentHashMap<T, Double>()
        gScore[aStarGraph.start] = 0.0

        val fScore = ConcurrentHashMap<T, Double>()
        fScore[aStarGraph.start] = aStarGraph.heuristic(aStarGraph.start)

        val cameFrom = ConcurrentHashMap<T, T>()

        val initialLoss = aStarGraph.getLoss(aStarGraph.start)
        var bestLoss = Double.POSITIVE_INFINITY
        val numEvaluations = AtomicInteger()

        val pollSyncObject = Object()

        var pathResult: List<T>? = null
        var costResult: Double? = null
        var errorResult: Throwable? = null

        while (openSet.isNotEmpty()) {
            val currentValues = (0..<min(parallelism, openSet.getSize())).map {
                val (currentF, current) = openSet.poll()
                currentF to current
            }

            Flux.fromIterable(currentValues)
                .flatMap { (currentF, current) ->
                    neighborCounter.compute(current) { _, count ->
                        (count ?: 0) - 1
                    }

                    val numEvals = numEvaluations.getAndIncrement()
                    if (numEvals == maxNumEvaluations) {
                        log("Ending A* at max of $maxNumEvaluations evals")
                        Mono.just(reconstructPath(cameFrom, aStarGraph.start, current) to currentF)
                    } else if (numEvals % 10000 == 0) {
                        synchronized(openSet) {
                            log(
                                "A*: $numEvals evals ${
                                    if (bestLoss.isFinite()) String.format(
                                        "%.03f",
                                        bestLoss
                                    ) else "inf"
                                }"
                            )
                        }
                        Mono.empty<Pair<List<T>, Double>>()
                    } else {
                        Mono.empty()
                    }
                        .switchIfEmpty(Mono.defer {
                            val loss = aStarGraph.getLoss(current)
                            synchronized(openSet) {
                                if (loss < bestLoss) {
                                    log("[${numEvals.toString().padStart(16)}] New best loss at $current: $loss / $initialLoss - $currentF")
                                    bestLoss = loss
                                }
                            }
                            if (loss < lossThreshold) {
                                Mono.just(reconstructPath(cameFrom, aStarGraph.start, current) to currentF)
                            } else {
                                Mono.empty()
                            }
                        })
                        .switchIfEmpty(Mono.fromCallable {
                            closedSet[current] = Unit

                            val currentNeighbors = aStarGraph.neighbors(current)

                            for ((neighbor, moveCost) in currentNeighbors) {
                                if (closedSet.containsKey(neighbor)) continue

                                val currentGScore = gScore[current] ?: Double.POSITIVE_INFINITY
                                val tentativeGScore = currentGScore + moveCost

                                val neighborGScore = gScore[neighbor] ?: Double.POSITIVE_INFINITY
                                if (tentativeGScore < neighborGScore) {
                                    cameFrom[neighbor] = current
                                    gScore[neighbor] = tentativeGScore
                                    fScore[neighbor] = tentativeGScore + aStarGraph.heuristic(neighbor)
                                    val existingNeighborCount = neighborCounter[neighbor]
                                    if (existingNeighborCount == null || existingNeighborCount == 0) {
                                        val fScoreValue = fScore[neighbor] ?: Double.POSITIVE_INFINITY
                                        openSet.add(fScoreValue to neighbor)
                                        neighborCounter[neighbor] = 1
                                    }
                                } else {
                                    log("Discarding expensive neighbor $neighbor [cost=${tentativeGScore} existing_cost=${neighborGScore}]")
                                }
                            }
                        }
                            .then<Pair<List<T>, Double>?>(Mono.empty())
                            .subscribeOn(Schedulers.boundedElastic())
                        )
                }
                .doOnNext { (path, cost) ->
                    synchronized(pollSyncObject) {
                        pathResult = path
                        costResult = cost
                    }
                }
                .doOnError {
                    synchronized(openSet) {
                        errorResult = it
                    }
                }
                .then()
                .doOnTerminate {
                    synchronized(pollSyncObject) {
                        pollSyncObject.notify()
                    }
                }
                .doOnCancel {
                    synchronized(pollSyncObject) {
                        pollSyncObject.notify()
                    }
                }
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe()

            synchronized(pollSyncObject) {
                try {
                    pollSyncObject.wait(Duration.ofMinutes(60L).toMillis())
                } catch (e: InterruptedException) {
                    logger.warn("A* waiting process interrupted")
                }
            }

            if (pathResult != null && costResult != null) {
                log("A* will finish due to found result")
                break
            } else if (openSet.isEmpty()) {
                log("A* will finish due to exhaustion of nodes")
                currentValues.lastOrNull()?.let {
                    pathResult = reconstructPath(cameFrom, aStarGraph.start, it.second)
                    costResult = it.first
                }
            }
        }

        if (errorResult != null) {
            log("Ending A* with error")

            throw errorResult!!
        } else if (pathResult != null && costResult != null) {
            log("Ending A* with result values [loss=${String.format("%.03f", bestLoss)} cost=${String.format("%.03f", costResult)}]")

            return pathResult to costResult
        } else {
            log("Ending A* at ${numEvaluations.get()} evals; exhausted search")
            return null to null
        }
    }
}

