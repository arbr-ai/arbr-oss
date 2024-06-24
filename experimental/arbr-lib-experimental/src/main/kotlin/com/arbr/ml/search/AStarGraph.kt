package com.arbr.ml.search

interface AStarGraph<T> {
    val start: T
    fun neighbors(node: T): List<Pair<T, Double>>
    fun heuristic(node: T): Double
    fun getLoss(node: T): Double
}