package com.arbr.og.object_model.common.functions.platform

interface DistanceComparable<E: DistanceComparable<E>> {
    fun distance(other: E): Double
}