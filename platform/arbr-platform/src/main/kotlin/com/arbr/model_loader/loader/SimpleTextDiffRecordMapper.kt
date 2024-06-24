package com.arbr.model_loader.loader

import com.arbr.content_formats.format.DiffLiteralPatchSectionSerializer
import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.content_formats.format.DiffLiteralSourceDocumentTokenizer
import com.arbr.content_formats.format.DiffParsedPatchSection
import com.arbr.content_formats.format.tokenizer.TokenFormatter
import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import com.arbr.util_common.hashing.HashUtils

/**
 * Record mapper for simple text diff alignment where the target is aligned to the base document; patch is ignored
 */
class SimpleTextDiffRecordMapper: DatasetRecordMapper<GitHubPublicNoisedPatchInfo, DiffPatchTestCase> {
    private val documentTokenizer = DiffLiteralSourceDocumentTokenizer()
    private val diffLiteralPatchSectionSerializer = DiffLiteralPatchSectionSerializer()
    private val tokenFormatter = TokenFormatter.plain<DiffParsedPatchSection>()

    override fun mapRecord(record: GitHubPublicNoisedPatchInfo): DiffPatchTestCase {
        val hash = HashUtils.sha1Hash(
            record.baseDocument,
            record.patchContent,
            record.fileContent,
        )

        val testCaseName = listOf(
            "st",
            record.repoId,
            record.pullRequestId,
            record.commitSha.takeLast(4),
            record.fileSha.takeLast(4),
        ).joinToString("-") { it.toString() }

        val targetDocument = DiffLiteralSourceDocument(record.fileContent)
        val documentTokens = documentTokenizer.tokenize(targetDocument)
        val documentPatchSection = DiffParsedPatchSection(
            lineStart = null,
            lineEnd = null,
            targetLineStart = null,
            targetLineEnd = null,
            operations = documentTokens,
        )
        val documentLiteralPatch = diffLiteralPatchSectionSerializer.serializeWith(
            LinearOrderList(listOf(documentPatchSection)),
            tokenFormatter
        )

        return DiffPatchTestCase(
            hash,
            testCaseName,
            DiffPatchDatasetKind.CLEAN,
            DiffLiteralSourceDocument(record.baseDocument),
            documentLiteralPatch,
            targetDocument,
            emptyMap(),
        )
    }
}