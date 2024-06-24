package com.topdown.parsers.lang.universal.tree_parsing.util

import org.antlr.v4.runtime.ParserRuleContext

object ParserRuleContexts {
    fun <T: ParserRuleContext> findFirstChildrenOfType(ctx: ParserRuleContext, childClass: Class<T>): List<T> {
        var children = ctx.children
        while (children.isNotEmpty()) {
            val found = children.filterIsInstance(childClass)
            if (found.isNotEmpty()) {
                return found
            }

            children = children.flatMap { (0 until it.childCount).map { i -> it.getChild(i) } }
        }
        return emptyList()
    }

    fun joinIdentifiers(identifiers: List<ParserRuleContext>): String {
        return if (identifiers.isEmpty()) {
            "<unknown>"
        } else if (identifiers.size == 1) {
            identifiers.first().text
        } else {
            val joinedText = identifiers.joinToString(", ") { it.text }

            "[$joinedText]"
        }
    }
}