package com.arbr.graphql.converter.expressions

data class CNode<D: DType, E: Expr<E>>(
    val dType: D,
    val expr: E,
) {
    companion object {
        fun <D: DType, E: Expr<E>> of(
            dType: D,
            expr: E,
        ): CNode<D, E> {
            return CNode(dType, expr)
        }
    }
}
