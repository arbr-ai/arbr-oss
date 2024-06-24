package com.arbr.model_loader.loader

import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo

class CleanDiffRecordFilter : DatasetRecordFilter<GitHubPublicNoisedPatchInfo> {
    private val textLengthLimit = 4000

    override fun shouldInclude(record: GitHubPublicNoisedPatchInfo): Boolean {
        return (record.baseDocument.length < textLengthLimit
                && record.fileContent.length < textLengthLimit
                && record.patchContent.length < textLengthLimit
                ) && record.baseDocument != record.fileContent
    }
}