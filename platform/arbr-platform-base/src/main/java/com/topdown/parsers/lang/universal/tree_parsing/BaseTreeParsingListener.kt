package com.topdown.parsers.lang.universal.tree_parsing

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.TerminalNode
import kotlin.math.max
import kotlin.math.min

abstract class BaseTreeParsingListener(
    private val contentLength: Long,
): ParseTreeListener {

    abstract fun <T: ParserRuleContext> getTerminalRule(ctx: T): TreeParsingTerminalRule<*>?

    var tree: ParserRuleTree? = null
        private set

    val listeners = mutableListOf<ParseTreeListener>()

    private fun <T : ParserRuleContext> getContextName(ctx: T): String? {
        return getTerminalRule(ctx)?.let { rule ->
            @Suppress("UNCHECKED_CAST")
            (rule as? TreeParsingTerminalRule<T>)?.getName?.let { it(ctx) }
        }
    }

    private fun pushRule(
        ruleName: String,
        contextName: String?,
        interval: Pair<Int, Int>,
    ) {
        val newTree = ParserRuleTree(
            ruleName,
            contextName,
            interval,
            tree,
            mutableListOf()
        )
        tree?.children?.add(newTree)
        tree = newTree
    }

    private fun popRule() {
        tree = tree?.parent
    }

    override fun visitTerminal(node: TerminalNode) {
        listeners.forEach { it.visitTerminal(node) }
    }

    override fun visitErrorNode(node: ErrorNode) {
        listeners.forEach { it.visitErrorNode(node) }
    }

    protected fun humanContextName(ctx: ParserRuleContext): String {
        val simpleClassName = (ctx::class.java.simpleName).split(".").last()

        val badWords = listOf("Declaration", "Expression", "Statement", "Context")
        val filteredClassName = badWords.fold(simpleClassName) { s, badWord -> s.replace(badWord, "") }

        return filteredClassName.withIndex().joinToString("") {(i, c) ->
            if (i == 0) {
                c.lowercase()
            } else if (c.isUpperCase()) {
                "_${c.lowercase()}"
            } else {
                c.toString()
            }
        }
    }

    open fun getIndexRange(ctx: ParserRuleContext, terminalRule: TreeParsingTerminalRule<*>?): Pair<Int, Int> {
        // I don't understand why the stopIndex is sometimes lower than the startIndex - it seems like they
        // just need to be reversed in these cases
        // Note start and stop are null for some rules - if we want any of these as terminal rules, will need to handle
        // that case specially
        val startIndex: Int
        val stopIndex: Int
        val startToken = ctx.start
        val stopToken = ctx.stop
        if (startToken != null && stopToken != null) {
            startIndex = max(0, min(startToken.startIndex, stopToken.stopIndex))
            stopIndex = max(startToken.startIndex, stopToken.stopIndex)
        } else if (startToken == null && stopToken == null) {
            if (terminalRule != null && tree != null) {
                val contextName = getContextName(ctx) ?: humanContextName(ctx)
                throw Exception("Missing start and stop index for rule ${terminalRule.humanRuleName} $contextName")
            }

            startIndex = 0
            stopIndex = contentLength.toInt()
        } else if (stopToken != null) {
            stopIndex = stopToken.stopIndex
            startIndex = stopIndex
        } else {
            startIndex = max(0, startToken.startIndex)
            stopIndex = startIndex
        }

        return startIndex to stopIndex
    }

    override fun enterEveryRule(ctx: ParserRuleContext) {
        val terminalRule = getTerminalRule(ctx)

        val (startIndex, stopIndex) = getIndexRange(ctx, terminalRule)
        val ruleName = terminalRule?.humanRuleName ?: defaultWholeFileHumanRuleName
        val contextName = getContextName(ctx) ?: humanContextName(ctx)

        if (terminalRule != null || tree == null) {
            pushRule(ruleName, contextName, startIndex to stopIndex)
            listeners.forEach { it.enterEveryRule(ctx) }
        }
    }

    override fun exitEveryRule(ctx: ParserRuleContext) {
        // Small hack: Don't pop all the way to null, in case the root is a rule
        if (getTerminalRule(ctx) != null && tree?.parent != null) {
            popRule()
            listeners.forEach { it.exitEveryRule(ctx) }
        }
    }

    companion object {
        private const val defaultWholeFileHumanRuleName = "whole_file"
    }

}