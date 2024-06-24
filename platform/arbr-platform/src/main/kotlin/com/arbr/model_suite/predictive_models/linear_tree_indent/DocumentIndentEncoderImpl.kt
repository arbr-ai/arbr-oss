package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.arbr.platform.ml.linear.typed.impl.TypedMatrices
import com.arbr.platform.ml.linear.typed.shape.Dim

class DocumentIndentEncoderImpl(
    private val commonVocabulary: DocumentVocabulary,
) : DocumentIndentEncoder {
    override fun <DocumentSize : Dim, FeatureDimensions : Dim> encode(
        documentModel: DocumentModel,
        documentSampleSizeShape: DocumentSize,
        featureDimensionsShape: FeatureDimensions
    ): TypedMatrices.Sample<DocumentSize, FeatureDimensions> {
        val tokenAverageIndentChangeMatrixArray = run {
            val singleTokenSample = documentModel.nodeInfo.map {
                commonVocabulary.encode(it.node.ruleName) to it.whitespaceUnitCountChange
            }
            val netIndents: Map<Int, Pair<Int, Int>> =
                singleTokenSample.groupingBy { it.first }.aggregate { _, accumulator, element, _ ->
                    if (accumulator == null) {
                        element.second to 1
                    } else {
                        (accumulator.first + element.second) to (accumulator.second + 1)
                    }
                }
            val netIndentAverages = netIndents.mapValues { (_, p) ->
                val (x, n) = p
                x * 1.0 / n
            }

            val tokenAverageIndentChangeMatrixArray = Array(commonVocabulary.size) { t ->
                DoubleArray(2) {
                    if (it == 0) {
                        netIndentAverages[t] ?: 0.0
                    } else {
                        1.0
                    }
                }
            }

            tokenAverageIndentChangeMatrixArray
        }

        val vectorize: (Int, LinearTreeIndentNodeInfo) -> Pair<DoubleArray, Double> = { _, rowNodeInfo ->

            val ancestorVector = commonVocabulary.makeWordVector(rowNodeInfo.ancestors)
//            val lineMateVector = commonVocabulary.makeWordVector(rowNodeInfo.lineMates)
//            val prevLineMateVector = commonVocabulary.makeWordVector(rowNodeInfo.prevLineTokenCodes) + rowNodeInfo.prevLineIndent.toDouble()
//            val postLineMateVector = commonVocabulary.makeWordVector(rowNodeInfo.postLineTokenCodes) + rowNodeInfo.postLineIndent.toDouble()
            val acVector = tokenAverageIndentChangeMatrixArray[commonVocabulary.encode(rowNodeInfo.node.ruleName)]

            val indents = rowNodeInfo.whitespaceUnitCount.toDouble()
            (ancestorVector + acVector) to indents
        }

        return TypedMatrices.makeSample(
            documentModel.nodeInfo,
            vectorize,
            documentSampleSizeShape,
            featureDimensionsShape,
        )
    }
}