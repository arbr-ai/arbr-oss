package com.arbr.model_suite.predictive_models.document_diff_alignment

import com.arbr.content_formats.format.DiffLiteralPatch
import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.platform.alignable.alignable.diff.DiffableDocumentState
import com.arbr.ml.optimization.base.ParameterValueProvider
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

internal class DocumentDiffAlignmentHelperDeferredImpl(
    private val parameterValueProviderMono: Mono<ParameterValueProvider>,
) : DocumentDiffAlignmentHelper {

    private val innerDocumentDiffAlignmentHelperMono = parameterValueProviderMono
        .map { parameterValueProvider ->
            DocumentDiffAlignmentHelperImpl(parameterValueProvider)
        }
        .cache()

    override fun alignAsync(
        patchTokens: DiffableDocumentState,
        documentTokens: DiffableDocumentState,
        innerTimeout: Duration?
    ): Mono<Optional<DiffableDocumentState>> {
        return innerDocumentDiffAlignmentHelperMono
            .flatMap { helper ->
                helper.alignAsync(patchTokens, documentTokens, innerTimeout)
            }
    }

    override fun alignBySectionAsync(
        patch: DiffLiteralPatch,
        document: DiffLiteralSourceDocument,
        innerTimeout: Duration?
    ): Mono<DiffableDocumentState> {
        return innerDocumentDiffAlignmentHelperMono
            .flatMap { helper ->
                helper.alignBySectionAsync(patch, document, innerTimeout)
            }
    }
}