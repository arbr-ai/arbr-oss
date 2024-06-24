package com.arbr.model_suite.predictive_models.document_diff_alignment

import com.arbr.content_formats.format.*
import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.arbr.data_structures_common.partial_order.PartialOrderFlatteningScheme
import com.arbr.platform.alignable.alignable.diff.AlignableDiffOperation
import com.arbr.platform.alignable.alignable.diff.DiffableDocumentState
import com.arbr.platform.alignable.alignable.v2.dag.AdjacencyMatrixDAGAlignmentHelper
import com.arbr.platform.alignable.alignable.v2.dag.AdjacencyMatrixDAGValued
import com.arbr.alignable.util.Either
import com.arbr.alignable.util.OperationLimitException
import com.arbr.ml.optimization.base.ParameterValueProvider
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.*
import kotlin.jvm.optionals.getOrElse

internal class DocumentDiffAlignmentHelperImpl(
    val parameterValueProvider: ParameterValueProvider,
) : DocumentDiffAlignmentHelper {
    private val sectionTokenizer = DiffParsedPatchSectionTokenizer()
    private val tokenizer = DiffLiteralSourceDocumentTokenizer()
    private val documentTokenizer = tokenizer.mapInto(
        LinearOrderList(emptyList()),
    ) { diffOperation ->
        AlignableDiffOperation(
            diffOperation,
            parameterValueProvider,
        )
    }

    private fun adjacencyMatrix(
        tokens: DiffableDocumentState,
    ): AdjacencyMatrixDAGValued<AlignableDiffOperation> {
        val n = tokens.size

        val matrix = (0 until n).map { i ->
            (0 until n).map { j ->
                i < j
            }
        }
        return AdjacencyMatrixDAGValued(
            matrix,
            tokens.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST),
        )
    }

    override fun alignAsync(
        patchTokens: DiffableDocumentState,
        documentTokens: DiffableDocumentState,
        innerTimeout: Duration?,
    ): Mono<Optional<DiffableDocumentState>> {
        val alignmentCache = DocumentDiffAlignmentCache()
        val inducer = DocumentDiffAlignmentInducer(alignmentCache, parameterValueProvider)

        return Mono.fromCallable {
            val target = adjacencyMatrix(patchTokens)
            val source = adjacencyMatrix(documentTokens)
            val helper = AdjacencyMatrixDAGAlignmentHelper(
                target,
                source,
                inducer,
                alignmentCache,
                parameterValueProvider,
            )

            helper
        }
            .flatMap { helper ->
                Mono.fromCallable {
                    try {
                        val alignmentPath = helper.alignmentPath()

                        val result = alignmentPath?.first?.mapNotNull {
                            it.latestOperation?.targetElement?.node
                        }?.let { LinearOrderList(it) }

                        val completeResult = result?.let { linearOrder ->
                            LinearOrderList(
                                linearOrder
                            )
                        }

                        Either.Left<Optional<DiffableDocumentState>, OperationLimitException>(
                            Optional.ofNullable(completeResult)
                        )
                    } catch (e: OperationLimitException) {
                        Either.Right(e)
                    }
                }.subscribeOn(Schedulers.boundedElastic())
                    .doOnCancel {
                        helper.cancel()
                    }
                    .let {
                        if (innerTimeout == null) {
                            it
                        } else {
                            it.timeout(innerTimeout)
                        }
                    }
                    .flatMap { either ->
                        either.mapEither(
                            { Mono.just(it) },
                            { Mono.error(it) }
                        )
                    }
            }
    }

    /**
     * Condense a document state between patch sections to remove DELs and simplify to NOPs.
     */
    private fun condenseInterstitialDocumentState(diffableDocumentState: DiffableDocumentState): DiffableDocumentState {
        return DiffableDocumentState(
            diffableDocumentState
                .filter { it.kind != DiffOperationKind.DEL }
                .map { ado ->
                    ado.copy(
                        diffOperation = ado.diffOperation.copy(
                            kind = DiffOperationKind.NOP
                        )
                    )
                }
        )
    }

    override fun alignBySectionAsync(
        patch: DiffLiteralPatch,
        document: DiffLiteralSourceDocument,
        innerTimeout: Duration?,
    ): Mono<DiffableDocumentState> {
        val patches = sectionTokenizer.tokenize(patch)
            .mapIndexed { i, section -> section to (i == 0) }

        // Wrap in alignable
        val initialDocumentTokens = documentTokenizer.tokenize(document)

        return patches.fold(Mono.just(initialDocumentTokens)) { runningDocumentPairMono, (patchSection, isFirst) ->
            runningDocumentPairMono
                .flatMap { diffableDocument ->
                    val simplifiedDocument = if (isFirst) {
                        diffableDocument
                    } else {
                        condenseInterstitialDocumentState(diffableDocument)
                    }

                    val alignableOperations = patchSection.operations
                        .map {
                            AlignableDiffOperation(it, parameterValueProvider)
                        }

                    alignAsync(alignableOperations, simplifiedDocument, innerTimeout)
                        .map { nextDocumentOpt ->
                            nextDocumentOpt.getOrElse { diffableDocument }
                        }
                }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DocumentDiffAlignmentHelper::class.java)
    }

}