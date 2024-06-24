package com.arbr.model_suite.predictive_models.document_diff_alignment

import com.arbr.content_formats.format.DiffLiteralPatch
import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.platform.alignable.alignable.diff.DiffableDocumentState
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

interface DocumentDiffAlignmentHelper {
    fun alignAsync(
        patchTokens: DiffableDocumentState,
        documentTokens: DiffableDocumentState,
        innerTimeout: Duration?,
    ): Mono<Optional<DiffableDocumentState>>

    fun alignBySectionAsync(
        patch: DiffLiteralPatch,
        document: DiffLiteralSourceDocument,
        innerTimeout: Duration?,
    ): Mono<DiffableDocumentState>

    fun alignBySectionAsync(
        patch: DiffLiteralPatch,
        document: DiffLiteralSourceDocument,
    ): Mono<DiffableDocumentState> {
        return alignBySectionAsync(
            patch,
            document,
            innerTimeout = null,
        )
    }
}
