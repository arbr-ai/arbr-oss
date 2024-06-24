package com.arbr.ml.search

import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.base.Matrix
import org.slf4j.LoggerFactory

object AStarTokenization {
    private val logger = LoggerFactory.getLogger(AStarTokenization::class.java)

    data class Result(
        val path: List<TokenizationDocumentModel>?,
        val contractionMatrix: Matrix<Dim.VariableN, Dim.VariableN>?,
        val cost: Double?,
    )

    fun tokenizeAStar(
        baseDocument: List<String>,
        branchingFactor: Int = 4,
        targetLoss: Double = 0.5,
        maxNumEvaluations: Int = 10_000,
        /**
         * Parallel to base document - assignment of tokens to predictive classes
         */
        tokenClasses: List<Boolean>? = null,
        logInfo: Boolean = true,
        parallelism: Int = 8,
    ): Result {
        val taggedTokens = if (tokenClasses == null) {
            baseDocument.map {
                TaggedToken(it, 0, 0)
            }
        } else {
            baseDocument.zip(tokenClasses).map {
                TaggedToken(it.first, if (it.second) 1 else 0, 1)
            }
        }

        val aStarGraph = AStarTokenizationGraph(taggedTokens, targetLoss, branchingFactor)

        val (path, totalCost) = AStar.aStarSearch(
            aStarGraph = aStarGraph,
            lossThreshold = targetLoss,
            maxNumEvaluations = maxNumEvaluations,
            logInfo = logInfo,
            parallelism = parallelism,
        )

        return if (path != null) {
            val tokenizations = aStarGraph.getTokenizations()
            val tokens = path.mapNotNull { tokenizations[it] }
//            val contractionMatrix = AStarTokenizationGraph.getTokenizationContractionGraph(
//                path.map {
//                    tokenizations[it]!!.contractions.last()
//                }
//            )
//
//            Result(
//                tokens,
//                contractionMatrix,
//                totalCost
//            )

            throw Exception()
        } else {
            Result(
                null,
                null,
                null,
            )
        }
    }

    fun tokenizeAStarEpochs(
        baseDocument: List<String>,
        branchingFactor: Int = 4,
        targetLoss: Double = 0.5,
        maxNumEvaluationsPerEpoch: Int = 10_000,
        numEpochs: Int = 4,
        /**
         * Parallel to base document - assignment of tokens to predictive classes
         */
        tokenClasses: List<Boolean>? = null,
        logInfo: Boolean = true,
        parallelism: Int = 8,
    ): Result {
        val finalPath = mutableListOf<TokenizationDocumentModel>()
        var totalCost = 0.0
        var tokenizedDocument = baseDocument

        throw Exception()

//        for (epoch in (0..<numEpochs)) {
//            logger.info("== Epoch $epoch ==")
//            val (path, contractionMatrix, cost) = tokenizeAStar(
//                tokenizedDocument,
//                branchingFactor,
//                targetLoss,
//                maxNumEvaluationsPerEpoch,
//                tokenClasses, // misaligned with tokenizedDocument after first epoch?
//                logInfo,
//                parallelism,
//            )
//            if (path == null || cost == null || path.isEmpty()) {
//                return Result(
//                    path,
//                    contractionMatrix,
//                    cost,
//                )
//            }
//            finalPath.addAll(path)
//            totalCost += cost
//
//            val lastDocument = path.last()
//            val stringTokens = lastDocument.tokenCodes.map { k ->
//                lastDocument.decoder[k]!!
//            }
//
//            tokenizedDocument = stringTokens
//            logger.info("Document # tokens after epoch: ${tokenizedDocument.size}")
//        }
//
//        val combinedMatrix = AStarTokenizationGraph.getTokenizationContractionGraph(finalPath.map { it.contractions.last() })
//
//        return Result(
//            finalPath,
//            combinedMatrix,
//            totalCost,
//        )
    }
}
