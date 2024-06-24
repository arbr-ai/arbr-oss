package com.arbr.graphql.converter.expressions

data class RenderPathContext(
    val depth: Int
)

/**
 * Type represented by an expression, e.g. a "Value", "Method", "VariableDeclaration", "LiteralType", etc.
 * Contextualized by language separately
 * Can be considered the "Formal" type
 */
sealed interface Expr<V : Expr<V>> {
    sealed interface Cn<V : Expr<V>> : Expr<V> {
        fun renderInto(
            target: Appendable,
            ctx: RenderPathContext,
        )

        fun render(ctx: RenderPathContext): String {
            val target = StringBuilder()
            renderInto(target, ctx)
            return target.toString()
        }
    }

    fun interface Fn<U : Expr<U>, V : Expr<V>> : Expr<Fn<U, V>> {
        fun apply(u: U): V
    }
}
