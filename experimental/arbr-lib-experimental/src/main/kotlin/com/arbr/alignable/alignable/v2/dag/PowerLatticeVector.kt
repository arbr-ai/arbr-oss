package com.arbr.alignable.alignable.v2.dag

import kotlin.math.absoluteValue
import kotlin.math.min

class PowerLatticeVector(
    val powerArray: Array<Int>,
) {
    val dim: Int get() = powerArray.size

    fun elementWiseMin(other: PowerLatticeVector): PowerLatticeVector {
        val arr = Array(dim) {
            min(powerArray[it], other.powerArray[it])
        }
        return PowerLatticeVector(arr)
    }

    fun elementWiseMax(other: PowerLatticeVector): PowerLatticeVector {
        val arr = Array(dim) {
            min(powerArray[it], other.powerArray[it])
        }
        return PowerLatticeVector(arr)
    }

    fun l1Norm(): Int {
        return powerArray.sumOf { it.absoluteValue }
    }

    override fun toString(): String {
        return "[" + powerArray.joinToString(",") { it.toString() } + "]"
    }

    companion object {
        fun unitVector(dim: Int, i: Int): PowerLatticeVector {
            return PowerLatticeVector(
                Array(dim) {
                    if (it == i) 1 else 0
                }
            )
        }
    }
}