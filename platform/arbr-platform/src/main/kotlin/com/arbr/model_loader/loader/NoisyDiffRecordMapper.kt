package com.arbr.model_loader.loader

import com.arbr.content_formats.format.DiffLiteralPatch
import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import com.arbr.util_common.hashing.HashUtils

class NoisyDiffRecordMapper(
    private val datasetKind: DiffPatchDatasetKind,
): DatasetRecordMapper<GitHubPublicNoisedPatchInfo, DiffPatchTestCase> {
    override fun mapRecord(record: GitHubPublicNoisedPatchInfo): DiffPatchTestCase {
        val hash = HashUtils.sha1Hash(
            record.baseDocument,
            record.patchContent,
            record.fileContent,
        )

        val testCaseName = listOf(
            "n",
            record.repoId,
            record.pullRequestId,
            record.commitSha,
            record.fileSha,
        ).joinToString("-") { it.toString() }

        return DiffPatchTestCase(
            hash,
            testCaseName,
            datasetKind,
            DiffLiteralSourceDocument(record.baseDocument),
            DiffLiteralPatch(record.patchContent),
            DiffLiteralSourceDocument(record.fileContent),
            mapOf(
                "model_version" to record.noiseModelVersion,
                "difficulty" to record.noiseAddedDifficulty,
            ),
        )
    }
}