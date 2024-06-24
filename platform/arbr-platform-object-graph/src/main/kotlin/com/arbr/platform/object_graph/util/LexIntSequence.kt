package com.arbr.platform.object_graph.util

import kotlin.math.max

// TODO: Move
class LexIntSequence(
    val ints: List<Int>
): Comparable<LexIntSequence> {
    /**
     * a - b
     */
    override fun compareTo(other: LexIntSequence): Int {
        val maxLen = max(ints.size, other.ints.size)
        for (i in 0 until maxLen) {
            if (i == ints.size && i == other.ints.size) {
                return 0
            }
            if (i == ints.size) {
                // Prefix
                return -1
            }
            if (i == other.ints.size) {
                // Other is prefix of this
                return 1
            }

            val thisVal = ints[i]
            val otherVal = other.ints[i]
            if (thisVal > otherVal) {
                return 1
            }
            if (otherVal > thisVal) {
                return -1
            }
        }

        // Eq
        return 0
    }
}