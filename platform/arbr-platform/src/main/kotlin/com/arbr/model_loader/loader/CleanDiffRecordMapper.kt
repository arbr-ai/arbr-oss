package com.arbr.model_loader.loader

import com.arbr.util_common.hashing.HashUtils
import com.arbr.content_formats.format.DiffLiteralPatch
import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo

class CleanDiffRecordMapper: DatasetRecordMapper<GitHubPublicNoisedPatchInfo, DiffPatchTestCase> {
    override fun mapRecord(record: GitHubPublicNoisedPatchInfo): DiffPatchTestCase {
        val hash = HashUtils.sha1Hash(
            record.baseDocument,
            record.patchContent,
            record.fileContent,
        )

        val testCaseName = listOf(
            "c",
            record.repoId,
            record.pullRequestId,
            record.commitSha.takeLast(4),
            record.fileSha.takeLast(4),
        ).joinToString("-") { it.toString() }

        return DiffPatchTestCase(
            hash,
            testCaseName,
            DiffPatchDatasetKind.CLEAN,
            DiffLiteralSourceDocument(record.baseDocument),
            DiffLiteralPatch(record.patchContent),
            DiffLiteralSourceDocument(record.fileContent),
            emptyMap(),
        )
    }
}