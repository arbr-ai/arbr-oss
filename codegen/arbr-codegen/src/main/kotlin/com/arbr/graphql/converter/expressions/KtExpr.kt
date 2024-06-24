@file:Suppress("MemberVisibilityCanBePrivate")

package com.arbr.graphql.converter.expressions

sealed interface KtExpr {
    sealed interface Cn<V : Expr<V>> : Expr.Cn<V>, KtExpr

    class Value(
        private val literalRender: (Appendable, RenderPathContext) -> Unit,
    ) : KtExpr.Cn<Value> {
        constructor(literalValue: String) : this({ t, _ -> t.append(literalValue) })

        override fun renderInto(target: Appendable, ctx: RenderPathContext) {
            literalRender(target, ctx)
        }
    }

    class MethodParameter(
        val name: String,
        val literalType: LiteralType,
    ) : KtExpr.Cn<MethodParameter> {
        override fun renderInto(target: Appendable, ctx: RenderPathContext) {
            target.append(name)
            target.append(" : ")
            literalType.renderInto(target, ctx)
        }
    }

    class MethodHeader(
        private val modifiers: List<String>,
        val methodName: String,
        val methodParameters: List<MethodParameter>,
        private val returnType: LiteralType?,
    ) : KtExpr.Cn<MethodHeader> {
        override fun renderInto(target: Appendable, ctx: RenderPathContext) {
            modifiers.forEach {
                target.append(it)
                target.append(" ")
            }
            target.append("fun ")
            target.append(methodName)
            target.append("(")
            val numParameters = methodParameters.size
            if (numParameters > 0) {
                target.appendLine()
            }
            val wrappedTarget = IndentAppendableWrapper(target)
            methodParameters.forEach { methodParameter ->
                methodParameter.renderInto(wrappedTarget, ctx.copy(depth = ctx.depth + 1))
                wrappedTarget.append(",\n")
            }
            target.append(")")
            returnType?.let { re ->
                target.append(": ")
                re.renderInto(target, ctx)
            }

        }
    }

    class MethodImpl(
        private val methodHeader: MethodHeader,
        private val bodyExpressions: List<KtExpr.Cn<*>>,
        private val valuedReturn: Value?,
    ) : KtExpr.Cn<MethodImpl> {

        override fun renderInto(target: Appendable, ctx: RenderPathContext) {
            methodHeader.renderInto(target, ctx)
            target.appendLine(" {")
            val methodBodyTarget = IndentAppendableWrapper(target)
            bodyExpressions.forEach { expr ->
                expr.renderInto(methodBodyTarget, ctx)
                methodBodyTarget.appendLine()
            }

            valuedReturn?.let { vr ->
                methodBodyTarget.append("return ")
                vr.renderInto(methodBodyTarget, ctx)
                methodBodyTarget.appendLine()
            }

            target.appendLine("}")
        }
    }

    sealed interface LiteralType : KtExpr.Cn<LiteralType> {

        data class AtomicType(
            val literalType: String
        ) : LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append(literalType)
            }
        }

        // Could be generalized but we aren't supporting other parametric types
        data class NullableType<T : LiteralType>(
            val innerType: T
        ) : LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append("${innerType.render(ctx)}?")
            }
        }

        data class Erased1ParameterType<T : LiteralType>(
            val outerType: T
        ) : LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append("${outerType.render(ctx)}<*>")
            }
        }

        data class ListType<T : LiteralType>(
            val innerType: T
        ) : LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append("List<${innerType.render(ctx)}>")
            }
        }

        // Could be generalized but we aren't supporting other parametric types
        data class MapType<K : LiteralType, V : LiteralType>(
            val innerKeyType: K,
            val innerValueType: V,
        ) : LiteralType {
            override fun renderInto(target: Appendable, ctx: RenderPathContext) {
                target.append("Map<${innerKeyType.render(ctx)}, ${innerValueType.render(ctx)}>")
            }
        }

    }

    companion object {
        fun value(innerValue: Value, wrap: (String) -> String): Value {
            return Value { _, ctx ->
                wrap(innerValue.render(ctx))
            }
        }
    }
}