package com.arbr.graphql.converter.expressions

sealed interface GqlExpr {
    sealed interface Cn<V : Expr.Cn<V>>: Expr.Cn<V>, GqlExpr

    data class Value(
        private val literalRender: (Appendable, RenderPathContext) -> Unit,
    ) : GqlExpr.Cn<Value> {
        constructor(literalValue: String): this({ t, _ -> t.append(literalValue) })

        override fun renderInto(target: Appendable, ctx: RenderPathContext) {
            literalRender(target, ctx)
        }
    }

    sealed interface LiteralType: GqlExpr.Cn<LiteralType> {

        data class AtomicType(
            val literalType: String
        ): LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append(literalType)
            }
        }

        // Could be generalized but we aren't supporting other parametric types
        data class NullableType<T: LiteralType>(
            val innerType: T
        ): LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                innerType.renderInto(target, ctx)
            }
        }

        data class Erased1ParameterType<T: LiteralType>(
            val outerType: T
        ): LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append("${outerType.render(ctx)}<*>")
            }
        }

        data class ListType<T: LiteralType>(
            val innerType: T
        ): LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append("java.util.List<${innerType.render(ctx)}>")
            }
        }

        data class MapType<K: LiteralType, V: LiteralType>(
            val innerKeyType: K,
            val innerValueType: V,
        ): LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append("java.util.Map<${innerKeyType.render(ctx)}, ${innerValueType.render(ctx)}>")
            }
        }

    }
}