package com.arbr.types.homotopy.util

import com.arbr.types.homotopy.functional.HTypeReducer
import com.arbr.types.homotopy.functional.Reducer
import com.arbr.types.homotopy.util.StringReducer.Companion.StringBox

class HTypeStringReducer<Tr>(
    preOrderAppender: Appendable.(Tr, StringBox) -> Unit,
    postOrderAppender: Appendable.(Tr, StringBox) -> Unit,
) : HTypeReducer<Tr, Tr, String> {
    override val reducer: Reducer<Tr, String> = StringReducer.with(preOrderAppender, postOrderAppender)
}

class HTypeIndentingStringReducer<Tr>(
    private val preOrderAppender: Appendable.(Tr, StringBox) -> Unit,
    private val postOrderAppender: Appendable.(Tr, StringBox) -> Unit,
) : HTypeReducer<Tr, Tr, String> {

    private val wrappedPreOrder: Appendable.(Tr, StringBox) -> Unit = { tr, sb ->
        val appended = StringBuilder().run {
            preOrderAppender(this, tr, sb)
            toString()
        }
        if (appended.isNotEmpty()) {
            appended.lines().dropLast(1).forEach {
                append(sb.linePrefix)
                appendLine(it)
            }
            appended.lines().lastOrNull()?.takeIf { it.isNotEmpty() }?.let { append(it) }
            sb.linePrefix += "\t"
        }
    }
    private val wrappedPostOrder: Appendable.(Tr, StringBox) -> Unit = { tr, sb ->
        val appended = StringBuilder().run {
            postOrderAppender(this, tr, sb)
            toString()
        }
        if (appended.isNotEmpty()) {
            sb.linePrefix = sb.linePrefix.dropLast(1)
            appended.lines().dropLast(1).forEach {
                append(sb.linePrefix)
                appendLine(it)
            }
            appended.lines().lastOrNull()?.takeIf { it.isNotEmpty() }?.let { append(it) }
        }
    }

    override val reducer: Reducer<Tr, String> = StringReducer.with(
        wrappedPreOrder,
        wrappedPostOrder
    )
}