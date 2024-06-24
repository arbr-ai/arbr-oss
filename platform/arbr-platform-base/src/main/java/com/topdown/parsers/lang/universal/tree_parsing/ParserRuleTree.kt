package com.topdown.parsers.lang.universal.tree_parsing

data class ParserRuleTree(
    val ruleName: String,
    val contextName: String?,
    val interval: Pair<Int, Int>,
    val parent: ParserRuleTree?,
    val children: MutableList<ParserRuleTree>,
) {
    override fun toString(): String {
        val childStrs = children.joinToString("\n") {
            it.toString()
        }
        val childrenStr = childStrs.split("\n").joinToString("\n") { "|\t$it" }

        return "$ruleName $contextName ($interval)\n$childrenStr"
    }
}