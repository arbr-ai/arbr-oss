package com.arbr.ml.search

import com.arbr.platform.ml.linear.typed.base.Matrix
import com.arbr.platform.ml.linear.typed.impl.TypedMatrix
import com.arbr.platform.ml.linear.typed.shape.Dim
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ln

class AStarTokenizationGraph(
    private val baseDocument: List<TaggedToken>,
    private val targetLoss: Double,
    private val branchingFactor: Int = 4,
) : AStarGraph<Int> {
    override val start: Int = 0

    private val tokenizeByTagPrediction = baseDocument.any { it.positivity > 0 || it.negativity > 0 }

    private val baseDocumentModel: TokenizationDocumentModel = TokenizationDocumentModel
        .fromBaseDocument(
            baseDocument,
        )

    private val tokenizations = ConcurrentHashMap<Int, TokenizationDocumentModel>()
        .also {
            it[0] = baseDocumentModel
        }

    fun getTokenizations(): Map<Int, TokenizationDocumentModel> {
        return tokenizations
    }

    /**
     * Singular/structural information measure I[k; n] = k/n (-ln k/n)
     *  = k/n (ln n - ln k)
     */
    private fun informationMeasure(k: Int, n: Int): Double {
        if (k == 0) {
            // Zero probabilities show up for small samples, but we don't want to give weight to their impossible
            // events
            return 0.0
        }

        if (n == 0) {
            throw Exception("Cannot compute information for $k / 0")
        }

        val ratio = k * 1.0 / n
        val logN = ln(n.toDouble())
        val logK = ln(k.toDouble())

        return ratio * (logN - logK)
    }

    /**
     * Binary/indicator information measure for `(a, b)` disjoint categorical observation counts out of `n` total
     * events.
     * B[a, b; n] = (a+b)/n [-ln(ab/(a+b))]
     */
    private fun binaryInformationMeasure(a: Int, b: Int, n: Int): Double {
        var a1 = a
        var b1 = b

        if (a1 == 0 && b1 == 0) {
            // No information
            return 0.0
        }

        if (n == 0) {
            throw Exception("Cannot compute binary information for $a1, $b1 / 0")
        }

        if (a1 == 0 || b1 == 0) {
            // TODO: Consider alternatives
            // Pad both by 1; effect is only significant for small observations
            a1 += 1
            b1 += 1
        }

        val outerRatio = (a1 + b1) * 1.0 / n
        val ratio = a1 * 1.0 / (a1 + b1)
        val interiorProduct = ratio * (1 - ratio)

        if (interiorProduct <= 0) {
            throw Exception("Cannot compute binary information for $a1, $b1 / $n")
        }

        return -outerRatio * ln(interiorProduct)
    }

    private fun nextTokenizationCandidates(
        tokenizationDocumentModel: TokenizationDocumentModel,
    ): List<Pair<TokenizationContractionCandidate, Double>> {
//        val documentNumTokens = tokenizationDocumentModel.tokenCodes.size
//
//        // Limit to token pairs with at least two examples
//        val minFrequency = 2
//
//        val candidatePairings = tokenizationDocumentModel.pairings(minFrequency)
//
//        val scores = candidatePairings.map { candidate ->
//            val leftCode = candidate.contraction.leftCode
//            val rightCode = candidate.contraction.rightCode
//
//            val leftCountInitial = tokenizationDocumentModel.tokenCounts[candidate.contraction.leftCode]!!
//            val rightCountInitial = tokenizationDocumentModel.tokenCounts[candidate.contraction.rightCode]!!
//            val pairingCount = candidate.numOccurrences
//
//            // Final counts may not always be simply #initial - #pairing, namely when the pairing is between the same
//            // token class
//            val leftCountFinal = candidate.resultingDocument.tokenCounts[candidate.contraction.leftCode] ?: 0
//            val rightCountFinal = candidate.resultingDocument.tokenCounts[candidate.contraction.rightCode] ?: 0
//
//            val singularInformation0 = informationMeasure(leftCountInitial, documentNumTokens)
//            val singularInformation1 = informationMeasure(rightCountInitial, documentNumTokens)
//            val pairedInformation = informationMeasure(pairingCount, documentNumTokens)
//            val remainingInformation0 = informationMeasure(leftCountFinal, documentNumTokens)
//            val remainingInformation1 = informationMeasure(rightCountFinal, documentNumTokens)
//            val netInformationLost0 = singularInformation0 - remainingInformation0
//            val netInformationLost1 = singularInformation1 - remainingInformation1
//            val netPairingCost = netInformationLost0 + netInformationLost1 - pairedInformation
//
//            val cost = if (tokenizeByTagPrediction) {
//                val posT0 = tokenizationDocumentModel.tokenPositivity[leftCode]!!
//                val negT0 = tokenizationDocumentModel.tokenNegativity[leftCode]!!
//                val posT1 = tokenizationDocumentModel.tokenPositivity[rightCode]!!
//                val negT1 = tokenizationDocumentModel.tokenNegativity[rightCode]!!
//                val pairPos = candidate.resultingDocument.tokenPositivity[candidate.contraction.code]!!
//                val pairNeg = candidate.resultingDocument.tokenNegativity[candidate.contraction.code]!!
//
//                val binaryInformation0 = binaryInformationMeasure(posT0, negT0, documentNumTokens)
//                val binaryInformation1 = binaryInformationMeasure(posT1, negT1, documentNumTokens)
//                val binaryInformationPaired = binaryInformationMeasure(pairPos, pairNeg, documentNumTokens)
//                val remainingBinaryInformation0 =
//                    binaryInformationMeasure(posT0 - pairPos, negT0 - pairNeg, documentNumTokens)
//                val remainingBinaryInformation1 =
//                    binaryInformationMeasure(posT1 - pairPos, negT1 - pairNeg, documentNumTokens)
//                val netBinaryInformationLost0 = binaryInformation0 - remainingBinaryInformation0
//                val netBinaryInformationLost1 = binaryInformation1 - remainingBinaryInformation1
//                val netBinaryPairingCost =
//                    netBinaryInformationLost0 + netBinaryInformationLost1 - binaryInformationPaired
//
//                netBinaryPairingCost
//            } else {
//                netPairingCost
//            }
//
//            Invariants.check {
//                require(cost.isFinite() && !cost.isNaN())
//            }
//
//            candidate to cost
//        }
//            .sortedByDescending { (_, cost) ->
//                cost
//            }
//            .take(branchingFactor)
//
//        return scores

        throw Exception()
    }

    override fun getLoss(node: Int): Double {
        throw Exception()

//        val documentModel = tokenizations[node]!!
//        return documentModel.tokenCodes.size.toDouble() / baseDocument.size
    }

    override fun heuristic(node: Int): Double {
        throw Exception()

//        val documentModel = tokenizations[node]!!
//        val currentNumTokens = documentModel.tokenCodes.size
//        val remainingTokensToMerge = currentNumTokens - baseDocument.size * targetLoss
//        val costMinimizingStepSize = 0.37 * currentNumTokens
//        val numSteps = (remainingTokensToMerge / costMinimizingStepSize).toInt()
//
//        val knRatio = costMinimizingStepSize / currentNumTokens
//        val kndRatio = costMinimizingStepSize / (currentNumTokens - costMinimizingStepSize)
//        val stepCostApprox = -2 * knRatio * ln(knRatio) + kndRatio * ln(kndRatio)
//
//        return numSteps * stepCostApprox
    }

    override fun neighbors(node: Int): List<Pair<Int, Double>> {
        val currentDocumentModel = tokenizations[node]!!
        val candidates = nextTokenizationCandidates(currentDocumentModel)

        val indexedCandidates = mutableListOf<Pair<Int, Double>>()
        for ((tokenContractionCandidate, cost) in candidates) {
            val nextCode = node * 31 + tokenContractionCandidate.contraction.code

            tokenizations.computeIfAbsent(nextCode) {
                indexedCandidates.add(nextCode to cost)
                tokenContractionCandidate.resultingDocument
            }
        }

        return indexedCandidates
    }

    companion object {
        /**
         * Get lower triangular matrix of token hierarchy
         */
        fun getTokenizationContractionGraph(contractions: List<TokenCodeContraction>): Matrix<Dim.VariableN, Dim.VariableN> {
            val n = contractions.size
            val adjacency = Array(n) {
                DoubleArray(n) {
                    0.0
                }
            }

            val contractionReverseMap = contractions.withIndex().associate { (i, t) ->
                t.code to i
            }

            for ((i, c) in contractions.withIndex()) {
                val leftChildIndex = contractionReverseMap[c.leftCode]
                if (leftChildIndex != null) {
                    adjacency[i][leftChildIndex] = 1.0
                }

                val rightChildIndex = contractionReverseMap[c.rightCode]
                if (rightChildIndex != null) {
                    adjacency[i][rightChildIndex] = 1.0
                }
            }

            return TypedMatrix(adjacency, Dim.VariableN, Dim.VariableN)
        }
    }

}