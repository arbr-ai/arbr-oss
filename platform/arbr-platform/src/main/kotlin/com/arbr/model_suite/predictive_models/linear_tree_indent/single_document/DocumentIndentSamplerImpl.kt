package com.arbr.model_suite.predictive_models.linear_tree_indent.single_document

import com.arbr.model_suite.predictive_models.linear_tree_indent.DocumentIndentEncoder
import com.arbr.model_suite.predictive_models.linear_tree_indent.DocumentModel
import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.impl.TypedMatrices
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.roundToInt

class DocumentIndentSamplerImpl<NumFeatures: Dim>(
    private val documentIndentEncoder: DocumentIndentEncoder,
    private val preTrainedWeights: ColumnVector<NumFeatures>,
    private val documentWeights: ColumnVector<NumFeatures>,
    override val documentModel: DocumentModel,
    private val numFeaturesShape: NumFeatures,
) : DocumentIndentSampler {
    override val whitespaceUnit: Int = documentModel.whitespaceUnit

    override fun predictSynchronous(input: DocumentIndentSamplerInput): DocumentIndentSamplerOutput {
        val indent = sample(input.targetLineIndex)
        return DocumentIndentSamplerOutput(indent)
    }

    override fun sample(targetLineIndex: Int): Int {
        val targetNodes = documentModel.nodeInfo.withIndex().filter { (_, nodeInfo) ->
            // Note this relies on the normalization process not changing line indices at all
            documentModel.lineIndex(nodeInfo.node.startIndex) == targetLineIndex
                    || documentModel.lineIndex(max(0, nodeInfo.node.endIndex - 1)) == targetLineIndex
        }
        val targetNodeIndices = targetNodes.map { it.index }

        if (documentModel.normalizedDocumentLines[targetLineIndex].isBlank()) {
            return 0
        }

        if (targetNodes.isEmpty()) {
            throw IllegalArgumentException("No token originating or terminating at $targetLineIndex")
        }

        val sample = documentIndentEncoder.encode<Dim.VariableM, NumFeatures>(documentModel, Dim.VariableM, numFeaturesShape)
        val targetInputs = TypedMatrices.getRows(sample.inputs, targetNodeIndices)

        val product = targetInputs.mult(preTrainedWeights)
        val toArray2 = product.asArray()
        val elementMax = toArray2.map { it[0] }.average()
        return elementMax.roundToInt()
    }

    override fun formatLine(targetLineIndex: Int): String {
        val line = documentModel.normalizedDocumentLines[targetLineIndex]
        return try {
            val newLeadingWhitespaceUnitCount = sample(targetLineIndex)
            val whitespace = " ".repeat(documentModel.whitespaceUnit * newLeadingWhitespaceUnitCount)
            whitespace + line.trimStart()
        } catch (e: IllegalArgumentException) {
            line
        } catch (e: Exception) {
            logger.warn("Unrecognized exception when whitespace-formatting line", e)
            line
        }
    }

    override fun format(targetLineIndices: List<Int>): String {
        val newLeadingWhitespaceUnits = targetLineIndices.associateWith { li ->
            try {
                sample(li)
            } catch (e: IllegalArgumentException) {
                null
            } catch (e: Exception) {
                logger.warn("Unrecognized exception when whitespace-formatting line", e)
                null
            }
        }

        val whitespaceUnitToken = " ".repeat(documentModel.whitespaceUnit)
        val lines = documentModel.normalizedDocumentLines

        return lines.withIndex().joinToString("\n") { (index, line) ->
            val newLeadingWhitespaceCount = newLeadingWhitespaceUnits[index]

            if (newLeadingWhitespaceCount == null) {
                line
            } else {
                whitespaceUnitToken.repeat(newLeadingWhitespaceCount) + line.trimStart()
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DocumentIndentSamplerImpl::class.java)
    }
}