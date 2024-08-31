package com.arbr.platform.object_graph.common.functions.platform

interface DistanceComparable<E: DistanceComparable<E>> {
    fun distance(other: E): Double
}