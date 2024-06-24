package com.arbr.model_suite.predictive_models.linear_tree_indent.single_document

import com.arbr.model_suite.predictive_models.base.PredictiveSynchronousModel
import com.arbr.model_suite.predictive_models.linear_tree_indent.DocumentModel

interface DocumentIndentSampler: PredictiveSynchronousModel<DocumentIndentSamplerInput, DocumentIndentSamplerOutput> {
    val documentModel: DocumentModel
    val whitespaceUnit: Int

    fun sample(
        targetLineIndex: Int,
    ): Int

    fun formatLine(
        targetLineIndex: Int,
    ): String

    fun format(
        targetLineIndices: List<Int>
    ): String
}