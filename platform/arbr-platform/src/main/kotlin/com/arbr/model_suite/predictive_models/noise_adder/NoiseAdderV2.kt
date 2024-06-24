package com.arbr.model_suite.predictive_models.noise_adder

import com.arbr.content_formats.format.*
import com.arbr.content_formats.tokens.TokenizationUtils
import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.arbr.data_structures_common.partial_order.PartialOrderFlatteningScheme
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentHelperImpl
import com.arbr.ml.optimization.base.ParameterValueProvider
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant
import kotlin.random.Random

class NoiseAdderV2 private constructor(
    private val markovChain: MarkovChain<String>,
    private val parameterValueProvider: ParameterValueProvider,
): NoiseAdder {
    private val tokenInterjectionProbability = 0.04
    private val contextLineFabricateProbability = 0.25
    private val contextLineDropProbability = 0.25
    private val nonWhitespaceOnly = true

    private val documentDiffAlignmentHelper = DocumentDiffAlignmentHelperImpl(parameterValueProvider)
    private val documentTokenizer = DiffLiteralSourceDocumentTokenizer()
    private val patchSectionTokenizer = DiffParsedPatchSectionTokenizer()
    private val patchSectionSerializer = DiffLiteralPatchSectionSerializer()

    private fun addTextNoise(patch: String, tokenInterjectionProbability: Double): String {
        val patchTokens = TokenizationUtils.tokenize(patch)

        if (patchTokens.size < windowSize) {
            return patch
        }

        val newSequence = patchTokens.take(windowSize).toMutableList()
        val remainingTokens = patchTokens.drop(windowSize)
        for (token in remainingTokens) {
            val next = if (token.isBlank() && nonWhitespaceOnly) {
                token
            } else if (random.nextDouble() < tokenInterjectionProbability) {
                markovChain.sample(newSequence)
                    ?: token
            } else {
                token
            }

            newSequence.add(next)
        }

        return newSequence.joinToString("") { it }
    }

    private fun generateLine(after: String): String {
        val priorTokens = TokenizationUtils.tokenize(after)
        val extendedSequence = markovChain.sampleUntil(priorTokens, 32) {
            it.contains("\n")
        } ?: priorTokens
        val newSequence = extendedSequence.drop(priorTokens.size)
        return newSequence.joinToString("") { it }
    }

    override fun addNoise(
        baseDocument: String,
        patch: String,
    ): Flux<Pair<String, NoiseAddedDifficulty>> {
        val pairListMono = Mono.fromCallable {
            val patchSections = patchSectionTokenizer.tokenize(DiffLiteralPatch(patch))
            val documentTokens = documentTokenizer.tokenize(DiffLiteralSourceDocument(baseDocument))

            NoiseAddedDifficulty.values().map { difficulty ->
                val contextLineFabricateProbability = difficulty.scaleProbability(contextLineFabricateProbability)
                val contextLineDropProbability = difficulty.scaleProbability(contextLineDropProbability)
                val tokenInterjectionProbability = difficulty.scaleProbability(tokenInterjectionProbability)

                val noisedSections = patchSections.map { patchSection ->
                    val patchSectionOperations =
                        patchSection.operations.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST)

                    val firstEditOpIndex = patchSectionOperations.withIndex().firstOrNull { (_, op) ->
                        op.kind in listOf(DiffOperationKind.ADD, DiffOperationKind.DEL)
                    }?.index ?: 0
                    val lastEditOpIndex = patchSectionOperations.withIndex().lastOrNull { (_, op) ->
                        op.kind in listOf(DiffOperationKind.ADD, DiffOperationKind.DEL)
                    }?.index ?: patchSectionOperations.size

                    val newOperations = mutableListOf<DiffOperation>()

                    for ((i, operation) in patchSectionOperations.withIndex()) {
                        if (i <= firstEditOpIndex || i > lastEditOpIndex) {
                            // Consider injecting a context line
                            if (random.nextDouble() < contextLineFabricateProbability) {
                                val priorText = newOperations
                                    .joinToString("\n") { it.lineContent }
                                    .ifEmpty {
                                        // Look for a preceding literal lenient match line in the doc
                                        val documentPrior = if (firstEditOpIndex > 0) {
                                            val lastNonEditOp = patchSectionOperations[firstEditOpIndex - 1]
                                            val patchLineTextLenient = lastNonEditOp.lineContent.lowercase().trim()

                                            if (patchLineTextLenient.isBlank()) {
                                                null
                                            } else {
                                                val matchIndex = documentTokens.withIndex().firstOrNull { (_, line) ->
                                                    line.lineContent.lowercase().trim() == patchLineTextLenient
                                                }?.index

                                                if (matchIndex == null || matchIndex == 0) {
                                                    null
                                                } else {
                                                    documentTokens[matchIndex - 1]
                                                }
                                            }
                                        } else {
                                            null
                                        }

                                        documentPrior?.lineContent ?: ""
                                    }
                                    .trimEnd()
                                    .plus("\n")

                                // Don't fabricate lines based on empty text
                                if (priorText.isNotBlank()) {
                                    val newLineText = generateLine(priorText)

                                    val newOperation = DiffOperation(
                                        DiffOperationKind.NOP,
                                        newLineText,
                                        69000 + i,
                                    )
                                    newOperations.add(newOperation)
                                }
                            }

                            if (random.nextDouble() < contextLineDropProbability) {
                                continue
                            }
                        }

                        val newOperation = if (operation.kind == DiffOperationKind.ADD) {
                            operation
                        } else {
                            operation.copy(
                                lineContent = addTextNoise(operation.lineContent, tokenInterjectionProbability)
                            )
                        }

                        newOperations.add(newOperation)
                    }

                    patchSection.copy(operations = LinearOrderList(newOperations))
                }

                val serializedPatchSections = patchSectionSerializer.serialize(noisedSections)

                serializedPatchSections.text to difficulty
            }
        }.subscribeOn(Schedulers.boundedElastic())

        return pairListMono
            .flatMapIterable { it }
    }

    companion object {
        const val VERSION = "nv2_cl100k"

        private const val windowSize = 8

        private val random = Random(1328132909)

        private val logger = LoggerFactory.getLogger(NoiseAdderV2::class.java)

        fun train(
            documents: List<String>,
            parameterValueProvider: ParameterValueProvider,
        ): NoiseAdderV2 {
            logger.info("Training noise adder V2 on ${documents.size} documents totaling ${documents.sumOf { it.length } / 1000}KB...")

            val trainStartMs = Instant.now().toEpochMilli()

            val documentTokenSequences = documents.map { TokenizationUtils.tokenize(it) }
            logger.info("Tokenized Documents")

            val markovChain = MarkovChain.train(documentTokenSequences, windowSize)
            logger.info("Trained Markov Chain")

            val trainEndMs = Instant.now().toEpochMilli()
            logger.info("Completed NoiseAdder training in ${trainEndMs - trainStartMs}ms")

            return NoiseAdderV2(
                markovChain,
                parameterValueProvider,
            )
        }
    }
}