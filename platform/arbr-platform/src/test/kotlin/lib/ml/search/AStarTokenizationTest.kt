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

class AStarTokenizationTest {

    @BeforeEach
    fun init() {
        Invariants.setEnabled(true, failureLevel = Invariants.FailureLevel.THROW)
    }

    @Test
    fun `tokenizes via a star`() {
        val string =
            "aabbabaaabaaaacbaabbaacacbbaacbcabbbcaaaccacaacaaaaaabaabbcacaacccaaaaaaacbcabaabaaabaaaccbaabbaababbaabccbaababaacaaabcaabaaccacaabaabbbbaabbaabcacbaabaacbcabbaaccaacaaabbbcaaacbcabcaaacaaaaacaaabcabcacacaaabbbcaaacacbcaaacccacaabcabcaaaccacbaabbbabcaccacabaaaccccaaaccaaabaabbaa"

        val (tokenizationPath, cost) = AStarTokenization.tokenizeAStar(
            string.toList().map { it.toString() },
            branchingFactor = 4,
            targetLoss = 0.1,
            maxNumEvaluations = 100000,
        )
        println(cost)

        val documentModel = tokenizationPath!!.last()

        println(documentModel)
    }

    @Test
    fun `tokenizes to prediction`() {
        val units = listOf(
            "ab",
            "bc",
            "cb",
            "ca",
        )

        val random = Random(1498743L)
        val numWords = 16
        val numUnits = 64
        val magicString = "cab".repeat(24)

        val words = (0..<numWords).map { i ->
            (0..<numUnits).joinToString("") {
                units.random(random)
            }.let { w ->
                if (i % 2 == 0) {
                    w.take(w.length / 2 - (magicString.length / 2)) + magicString + w.drop(w.length / 2 + (magicString.length / 2))
                } else {
                    w
                }
            }
        }

        val classifications = words.map { w ->
            magicString in w
        }

        val charactersAndClassifications = words.zip(classifications).flatMap { (word, classification) ->
            word.toList().map { it.toString() to classification }
        }
        val chars = charactersAndClassifications.map { it.first }
        val singleClassifications = charactersAndClassifications.map { it.second }

        val targetLoss = 0.1
        val (tokenizationPath, _) = AStarTokenization.tokenizeAStarEpochs(
            chars,
            branchingFactor = 2,
            targetLoss = targetLoss,
            maxNumEvaluationsPerEpoch = 50000,
            numEpochs = 1,
            tokenClasses = null, // singleClassifications,
            logInfo = true,
            parallelism = 1,
        )
//        val tokenAggregates = tokenizationPath!!.map { it.aggregate }.filter { it.token.isNotEmpty() }
//        println(tokenAggregates.joinToString(", ") { "[$it]" })
//
//        val charsTaggedTokens = chars.map { TaggedToken(it, 0, 0) }
//        val tokenizer = AStarTokenizationGraph(charsTaggedTokens, targetLoss)
//        var runningDoc = charsTaggedTokens
//        for (t in tokenAggregates) {
//            runningDoc = tokenizer.tokenize(runningDoc, t).tokenizedDocument
//        }
//
//        val tokenClassCounters = mutableMapOf<String, Pair<Int, Int>>()
//        for ((word, classification) in words.zip(classifications)) {
//            val tokenizer0 = AStarTokenizationGraph(charsTaggedTokens, targetLoss)
//            var runningWord = word.map { TaggedToken(it.toString(), 0, 0) }
//            for (t in tokenAggregates) {
//                runningWord = tokenizer0.tokenize(runningWord, t).tokenizedDocument
//                assertEquals(word, runningWord.joinToString("") { it.token })
//            }
//
//            for (tk in runningWord) {
//                tokenClassCounters.compute(tk.token) { _, p ->
//                    val (pos, neg) = p ?: (0 to 0)
//                    if (classification) {
//                        (pos + 1) to neg
//                    } else {
//                        pos to (neg + 1)
//                    }
//                }
//            }
//        }
//        println(tokenClassCounters)
//
//        val infoSum = tokenClassCounters.entries.sumOf { (_, pair) ->
//            val (p, n) = pair
//            val p0 = max(p, 1)
//            val n0 = max(n, 1)
//            val prob = p0 * 1.0 / (p0 + n0)
//
//            -ln(prob * (1 - prob)) * (p + n)
//        } / tokenClassCounters.entries.sumOf { it.value.first + it.value.second }
//        println("Information quantity: $infoSum")

    }

    @Test
    fun `tokenizes to prediction uniform`() {
        val units = listOf(
            "abbc",
            "bfca",
            "cbda",
            "aadb",
        )

        val random = Random(13246743L)
        val numWords = 16
        val numUnits = 8
        val magicString = "cacbda"

        val words = (0..<numWords).map {
            (0..<numUnits).joinToString("") {
                units.random(random)
            }
        }

        val classifications = words.map { w ->
            magicString in w
        }

        val nsat = classifications.count { it }
        println("Positive cases: $nsat / ${classifications.size}")

        val charactersAndClassifications = words.zip(classifications).flatMap { (word, classification) ->
            word.toList().map { it.toString() to classification }
        }
        val chars = charactersAndClassifications.map { it.first }
        val singleClassifications = charactersAndClassifications.map { it.second }
        println("Starting with doc of ${chars.size} tokens")

        val targetLoss = 0.2
        val (tokenizationPath, _) = AStarTokenization.tokenizeAStar(
            chars,
            branchingFactor = 3,
            targetLoss = targetLoss,
            maxNumEvaluations = 100000,
            // numEpochs = 1,
            tokenClasses = singleClassifications,
            logInfo = true,
            parallelism = 1,
        )
//        val tokenAggregates = listOf("cacbdacbda") + tokenizationPath!!.map { it.aggregate }.filter { it.isNotEmpty() }
//        val tokenAggregates = tokenizationPath!!.map { it.aggregate }.filter { it.token.isNotEmpty() }
//        println(tokenAggregates.joinToString(", ") { "[$it]" })
//
//        val charsTaggedTokens = chars.map { TaggedToken(it, 0, 0) }
//        val tokenizer = AStarTokenizationGraph(charsTaggedTokens, targetLoss)
//        var runningDoc = charsTaggedTokens
//        for (t in tokenAggregates) {
//            runningDoc = tokenizer.tokenize(runningDoc, t).tokenizedDocument
//        }
//        println("Finished with doc of ${runningDoc.size} tokens")
//
//        val tokenClassCounters = mutableMapOf<String, Pair<Int, Int>>()
//        for ((word, classification) in words.zip(classifications)) {
//            val tokenizer0 = AStarTokenizationGraph(charsTaggedTokens, targetLoss)
//            var runningWord = word.map { TaggedToken(it.toString(), 0, 0) }
//            for (t in tokenAggregates) {
//                runningWord = tokenizer0.tokenize(runningWord, t).tokenizedDocument
//            }
//
//            assertEquals(word, runningWord.joinToString("") { it.token })
//
//            for (tk in runningWord) {
//                tokenClassCounters.compute(tk.token) { _, p ->
//                    val (pos, neg) = p ?: (0 to 0)
//                    if (classification) {
//                        (pos + 1) to neg
//                    } else {
//                        pos to (neg + 1)
//                    }
//                }
//            }
//        }
//        println(tokenClassCounters)
//
//        for ((word, classification) in words.zip(classifications)) {
//            val tokenizer0 = AStarTokenizationGraph(charsTaggedTokens, targetLoss)
//            var runningWord = word.map { TaggedToken(it.toString(), 0, 0) }
//            for (t in tokenAggregates) {
//                runningWord = tokenizer0.tokenize(runningWord, t).tokenizedDocument
//            }
//
//            assertEquals(word, runningWord.joinToString("") { it.token })
//
//            var pMin = 1.0
//            var pMax = 0.0
//            for (tk in runningWord) {
//                tokenClassCounters[tk.token]?.let { (pos, neg) ->
//                    if (pos == 0 && neg == 0) {
//                        //
//                    } else if (pos == 0) {
//                        // certain negative
//                        pMin = 0.0
//                    } else if (neg == 0) {
//                        // certain positive
//                        pMax = 1.0
//                    } else {
//                        val p = pos * 1.0 / (pos + neg)
//                        if (p < pMin) {
//                            pMin = p
//                        }
//                        if (p > pMax) {
//                            pMax = p
//                        }
//                    }
//                }
//            }
//
//            val score = 2 * (if (classification) pMax else 1 - pMin) - 1.0
//            println("Score $score for $word ($classification)")
//        }

//        val infoSum = tokenClassCounters.entries.sumOf { (_, pair) ->
//            val (p, n) = pair
//            val p0 = max(p, 1)
//            val n0 = max(n, 1)
//            val prob = p0 * 1.0 / (p0 + n0)
//
//            -ln(prob * (1 - prob)) * (p + n)
//        } / tokenClassCounters.entries.sumOf { it.value.first + it.value.second }
//        println("Information quantity: $infoSum")

    }

}