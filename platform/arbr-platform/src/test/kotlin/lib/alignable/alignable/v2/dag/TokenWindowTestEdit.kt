package com.arbr.alignable.alignable.v2.dag//package com.arbr.alignable.alignable.v2.dag
//
//import com.arbr.platform.alignable.alignable.alignment.Alignment
//import com.arbr.platform.alignable.alignable.edit_operation.AlignableEditOperation
//import com.arbr.data_structures_common.partial_order.KeyedValue
//
//data class TokenWindowTestEdit(
//    override val key: String,
//    val texts: List<String>
//) : AlignableEditOperation<TokenWindowTestState, TokenWindowTestEdit, TokenWindowEditAlignmentOp>, KeyedValue<String> {
//    override fun applyAlignment(alignmentOperations: List<TokenWindowEditAlignmentOp>): TokenWindowTestEdit {
//        return TokenWindowTestEdit(key, this.texts + alignmentOperations.map { it.text })
//    }
//
//    override fun align(e: TokenWindowTestEdit): Alignment<TokenWindowTestEdit, TokenWindowEditAlignmentOp> {
//        val newStrings = e.texts.toSet() - texts.toSet()
//        val ops = newStrings.map { TokenWindowEditAlignmentOp(it) }
//        return if (ops.isEmpty()) {
//            Alignment.Equal(this, e)
//        } else {
//            Alignment.Align(ops, ops.size.toDouble(), this, e)
//        }
//    }
//
//    override fun empty(): TokenWindowTestEdit {
//        return TokenWindowTestEdit(key, emptyList())
//    }
//
//    override fun applyTo(state: TokenWindowTestState): TokenWindowTestState {
//        val nextCode = state.code + texts
//            .map { it.hashCode() }
//            .reduce { acc, i -> acc * 31 + i }
//
//        return TokenWindowTestState(
//            state.priorTexts + texts,
//            state.priorCodes + state.code,
//            nextCode,
//        )
//    }
//}