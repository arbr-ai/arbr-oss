package com.arbr.ml.search

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.*
import kotlin.random.Random

class AStarTest {

    private val random = Random(41297862)

    data class Coord(
        val x: Double,
        val y: Double,
    )

    private val goal = Coord(10.0, 12.0)

    private fun graph(
        useHeuristic: Boolean = true,
        randomMovements: Boolean = true,
        stepSize: Double = 1.0,
        costModifier: (Pair<Coord, Double>) -> Double = { it.second },
    ): AStarGraph<Coord> = object : AStarGraph<Coord> {
        override val start: Coord = Coord(0.0, 0.0)

        override fun getLoss(node: Coord): Double {
            return sqrt((goal.x - node.x).pow(2) + (goal.y - node.y).pow(2))
        }

        override fun heuristic(node: Coord): Double {
            return if (useHeuristic) {
                sqrt((goal.x - node.x).pow(2) + (goal.y - node.y).pow(2))
            } else {
                0.0
            }
        }

        override fun neighbors(node: Coord): List<Pair<Coord, Double>> {
            val batchStepSize = 4

            val steps = if (randomMovements) {
                (0..<batchStepSize).map {
                    Coord(
                        node.x + (2 * random.nextDouble() - 1) * stepSize,
                        node.y + (2 * random.nextDouble() - 1) * stepSize,
                    )
                }
            } else {
                listOf(
                    Coord(
                        node.x - stepSize,
                        node.y,
                    ),
                    Coord(
                        node.x,
                        node.y - stepSize,
                    ),
                    Coord(
                        node.x + stepSize,
                        node.y,
                    ),
                    Coord(
                        node.x,
                        node.y + stepSize,
                    ),
                )
            }

            return steps.map { step ->
                val modifiedCost = costModifier(step to sqrt((step.x - node.x).pow(2) + (step.y - node.y).pow(2)))
                step to modifiedCost
            }
        }
    }

    @Test
    fun `finds path`() {
        val (coordPath, cost) = AStar.aStarSearch(
            graph(useHeuristic = true),
            lossThreshold = 0.1,
            maxNumEvaluations = 100_000
        )
        println(cost)
        println(coordPath)
    }

    @Test
    fun `finds path without heuristic`() {
        val (coordPath, cost) = AStar.aStarSearch(
            graph(useHeuristic = false),
            lossThreshold = 0.1,
            maxNumEvaluations = 200_000
        )
        println(cost)
        println(coordPath)
    }

    @Test
    fun `finds path with heights adding to cost`() {
        val graph = graph(
            useHeuristic = true,
            randomMovements = false,
            stepSize = 0.1,
        ) { (coord, baseCost) ->
            if (coord.x < -1 || coord.y < -1 || coord.x > 15 || coord.y > 15) {
                baseCost + 10.0
            } else {
                val dx = abs(coord.x - goal.x)
                val dy = abs(coord.y - goal.y)

                baseCost + (2 * cos(dx * PI / 2).absoluteValue + 2 * sin(dy * PI / 2).absoluteValue)
            }
        }

        val (coordPath, cost) = AStar.aStarSearch(
            graph,
            lossThreshold = 0.1,
            maxNumEvaluations = 200_000,
            logInfo = false,
        )
        println(cost)
        println(coordPath)
    }

    @Test
    fun `finds path with implied curvature`() {
        val graph = graph(
            useHeuristic = true,
            randomMovements = false,
            stepSize = 0.2,
        ) { (coord, baseCost) ->
            val xAlignFactor = 1 - exp(-abs(coord.x - goal.y)) // nullify added cost once x gets to goal y
            val curvatureFactor = abs(coord.y - coord.x.pow(2)) // coerce to y = x^2

            baseCost + xAlignFactor * curvatureFactor
        }

        val (coordPath, cost) = AStar.aStarSearch(
            graph,
            lossThreshold = 0.1,
            maxNumEvaluations = 200_0000,
            logInfo = false,
        )
        println(cost)
        for (coord in coordPath!!) {
            println("${String.format("%.03f", coord.x)}, ${String.format("%.03f", coord.y)}")
        }
    }

}