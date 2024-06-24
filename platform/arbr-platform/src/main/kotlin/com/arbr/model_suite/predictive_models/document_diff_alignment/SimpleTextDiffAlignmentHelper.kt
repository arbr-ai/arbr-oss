package com.arbr.model_suite.predictive_models.document_diff_alignment

import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.content_formats.format.DiffLiteralSourceDocumentTokenizer
import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.arbr.platform.alignable.alignable.diff.AlignableDiffOperation
import com.arbr.platform.alignable.alignable.diff.DiffableDocumentState
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.base.ParameterValueProvider
import com.arbr.ml.optimization.model.BindingParameter
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrNull


/**
 * Alignment helper variant for plain, symmetric, document-to-document diffs.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SimpleTextDiffAlignmentHelper(
    val parameterValueProvider: ParameterValueProvider,
) {
    private val tokenizer = DiffLiteralSourceDocumentTokenizer()
    private val alignableTokenizer = tokenizer.mapInto(
        LinearOrderList(emptyList()),
    ) { diffOperation ->
        AlignableDiffOperation(
            diffOperation,
            parameterValueProvider,
        )
    }

    fun align(
        targetTokens: List<AlignableDiffOperation>,
        baseTokens: List<AlignableDiffOperation>,
    ): Mono<DiffableDocumentState> {
        val helper = DocumentDiffAlignmentHelperImpl(
            parameterValueProvider
        )

        return helper.alignAsync(
            DiffableDocumentState(targetTokens),
            DiffableDocumentState(baseTokens),
            null,
        )
            .mapNotNull<DiffableDocumentState> {
                it.getOrNull()
            }
            .single()
    }

    fun align(
        targetContent: String,
        baseContent: String,
    ): Mono<DiffableDocumentState> {
        val targetTokens = alignableTokenizer.tokenize(
            DiffLiteralSourceDocument(targetContent)
        )
        val baseTokens = alignableTokenizer.tokenize(
            DiffLiteralSourceDocument(baseContent)
        )

        return align(targetTokens, baseTokens)
    }

    fun alignToDiffOperations(
        targetContent: String,
        baseContent: String,
    ): Mono<List<AlignableDiffOperation>> {
        return align(
            targetContent,
            baseContent
        ).map {
            it.toList()
        }
    }

    companion object {
        fun withParameterMap(
            parameterMap: Map<NamedMetricKind, BindingParameter<Double>>,
        ): SimpleTextDiffAlignmentHelper {
            TODO()

//            val parameterValueProvider = ParameterValueProviderImpl(
//                parameterMap,
//                defaultValue = null,
//            )
//
//            return SimpleTextDiffAlignmentHelper(
//                parameterValueProvider,
//            )
        }

        fun default(): SimpleTextDiffAlignmentHelper {
            TODO()

//            val parameterMap = listOf(
//                BindingParameter(MCONSTANT, 100.0),
//                BindingParameter(STR_WEIGHT, 100.0),
//                BindingParameter(STR_NORM, 0.0),
//                BindingParameter(DIFF_OP_BASELINE, 1.0),
//
//                BindingParameter(ADJ_MERGE, 1.0),
//                BindingParameter(ADJ_INDUCE, 1.0),
//                BindingParameter(ADJ_DEDUCE, 1.0),
//                BindingParameter(ADJ_ROOT_NODE, 1.0),
//                BindingParameter(ADJ_CHILD_NODE, 1.0),
//                BindingParameter(ADJ_EDGE_GENERAL, 1.0),
//
//                BindingParameter(TKW_DROP, 1E14),
//                BindingParameter(TKW_MATCH, 0.0),
//                BindingParameter(TKW_EDIT, 1E14),
//                BindingParameter(TKW_APPLY, 1E14),
//                BindingParameter(TKW_SKIP, 1E14),
//                BindingParameter(TKW_SKIP_PRE, 1E14),
//                BindingParameter(TKW_SKIP_POST, 1E14),
//
//                BindingParameter(OP_MATRIX_NUL_NUL, 0.0),
//                BindingParameter(OP_MATRIX_NUL_ADD, 9.9),
//                BindingParameter(OP_MATRIX_NUL_NOP, 9.9),
//                BindingParameter(OP_MATRIX_NUL_DEL, 9.9),
//                BindingParameter(OP_MATRIX_ADD_NUL, 9.9),
//                BindingParameter(OP_MATRIX_ADD_ADD, 0.0),
//                BindingParameter(OP_MATRIX_ADD_NOP, 1.0),
//                BindingParameter(OP_MATRIX_ADD_DEL, 9.9),
//                BindingParameter(OP_MATRIX_NOP_NUL, 9.9),
//                BindingParameter(OP_MATRIX_NOP_ADD, 1.0),
//                BindingParameter(OP_MATRIX_NOP_NOP, 0.0),
//                BindingParameter(OP_MATRIX_NOP_DEL, 1.0),
//                BindingParameter(OP_MATRIX_DEL_NUL, 9.9),
//                BindingParameter(OP_MATRIX_DEL_ADD, 9.9),
//                BindingParameter(OP_MATRIX_DEL_NOP, 1.0),
//                BindingParameter(OP_MATRIX_DEL_DEL, 0.0),
//            ).associateBy { it.metricKind }
//
//            return withParameterMap(parameterMap)
        }
    }
}
