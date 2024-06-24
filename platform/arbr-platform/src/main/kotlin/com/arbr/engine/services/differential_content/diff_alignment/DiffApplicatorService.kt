package com.arbr.engine.services.differential_content.diff_alignment

import com.arbr.content_formats.code.CodeBlockParser
import com.arbr.content_formats.format.DiffLiteralPatch
import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.content_formats.format.DiffOperation
import com.arbr.engine.services.differential_content.formatter.DocumentSerializerFactory
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentHelper
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DiffApplicatorService(
    private val documentDiffAlignmentHelper: DocumentDiffAlignmentHelper,
    private val documentSerializerFactory: DocumentSerializerFactory<DiffLiteralSourceDocument, DiffOperation>,
) {

    /**
     * Given base file content and natural-language completion output that should contain a loose diff, do the best we
     * can to extract a valid diff and apply it to the base file content. When in doubt, return the base file content.
     */
    fun extractAndApplyAlignedDiff(
        workflowHandleId: String,
        filePath: String,
        baseFileContent: String,
        completionOutput: String,
    ): Mono<String> {
        var codeBlocks = CodeBlockParser.parse(completionOutput)

        // If any code block is marked as a diff, limit to blocks tagged as diffs
        // Otherwise, interpret all blocks as potential diffs
        if (codeBlocks.any { it.tag?.lowercase() == "diff" }) {
            codeBlocks = codeBlocks.filter {
                it.tag?.lowercase() == "diff"
            }
        }

        return documentSerializerFactory
            .getCombinedDocumentSerializer(workflowHandleId, filePath).flatMap { combinedSerializer ->

                codeBlocks.fold(Mono.just(baseFileContent)) { contentMono, codeBlock ->
                    contentMono.flatMap { documentContent ->
                        val diffContent = codeBlock.lines.joinToString("\n")

                        documentDiffAlignmentHelper.alignBySectionAsync(
                            DiffLiteralPatch(diffContent),
                            DiffLiteralSourceDocument(documentContent),
                            innerTimeout = null,
                        ).map { alignedDocument ->
                            val newLiteralDocument =
                                combinedSerializer.serialize(alignedDocument.map { it.diffOperation })
                            newLiteralDocument.text
                        }
                    }
                }
            }
    }

}
