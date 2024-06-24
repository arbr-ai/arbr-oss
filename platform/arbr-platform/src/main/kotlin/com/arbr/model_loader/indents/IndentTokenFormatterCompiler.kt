package com.arbr.model_loader.indents

import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.content_formats.format.DiffOperation
import com.arbr.content_formats.format.DiffOperationKind
import com.arbr.content_formats.format.tokenizer.TokenFormatter
import com.arbr.data_structures_common.partial_order.PartialOrder
import com.arbr.model_suite.predictive_models.linear_tree_indent.LinearTreeIndentPredictor
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmenterService
import com.arbr.platform.ml.linear.typed.shape.Dim

class IndentTokenFormatterCompiler(
    private val segmenterService: SegmenterService,
    private val fileName: String,
    private val vocabularyList: List<String>,
) : TokenFormatterCompiler<DiffLiteralSourceDocument, DiffOperation> {
    override fun compileFormatter(
        tokens: PartialOrder<DiffOperation>,
        preSerializedTargetDocument: DiffLiteralSourceDocument
    ): TokenFormatter<DiffOperation> {
        val targetFileContent = preSerializedTargetDocument.text

        val sampler = LinearTreeIndentPredictor.compileSingleDocument<Dim.VariableN>(
            segmenterService,
            vocabularyList,
            fileName,
            targetFileContent,
            Dim.VariableN,
        )

        return TokenFormatter { lineIndex, operation ->
            if (operation.kind == DiffOperationKind.ADD) {
                val newLineText = sampler.formatLine(lineIndex)
                operation.copy(
                    lineContent = newLineText
                )
            } else {
                operation
            }
        }
    }
}

