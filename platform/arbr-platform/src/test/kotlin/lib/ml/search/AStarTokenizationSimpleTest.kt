package com.arbr.ml.search

import com.arbr.util_common.invariants.Invariants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.Assert
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class AStarTokenizationSimpleTest {

    @BeforeEach
    fun init() {
        Invariants.setEnabled(true, failureLevel = Invariants.FailureLevel.THROW)
    }

    @Test
    fun `tokenizes simple units`() {
        val units = listOf(
            "abcd",
            "efgh",
            "ijkl",
            "mnop",
        )

        val random = Random(1498743L)
        val numUnits = 256

        val text = (0..<numUnits).joinToString("") { units.random(random) }
        val initialTokens = text.toList().map { it.toString() }

        val (tokenizationPath, cost) = AStarTokenization.tokenizeAStar(
            initialTokens,
            branchingFactor = 1,
            targetLoss = 0.25,
            maxNumEvaluations = 100000,
        )
        println(cost)

//        val tokenAggregates = tokenizationPath!!.map { it.aggregate }.filter { it.token.isNotEmpty() }
//        println(tokenAggregates.joinToString("\n"))
    }

}