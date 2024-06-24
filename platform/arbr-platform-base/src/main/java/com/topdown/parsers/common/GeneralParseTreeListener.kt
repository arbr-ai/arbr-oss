package com.topdown.parsers.common

import com.topdown.parsers.common.model.GeneralParseTreeNodeModel
import com.topdown.parsers.common.model.RuleContextModel
import com.topdown.parsers.common.model.TerminalNodeModel
import com.topdown.parsers.util.DocumentLocusHelper
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

class GeneralParseTreeListener(
    val content: String,
) : ParseTreeListener {

    val documentLocusHelper = DocumentLocusHelper(content)

    private val pushedNodeList = mutableListOf<MutableNode>()
    private var poppedCounter = 0
    private val nodeStack = Stack<MutableNode>()
    private val ruleClassNames = mutableMapOf<Int, String>()

    fun getTreeModel(): List<GeneralParseTreeNodeModel> {
        return pushedNodeList.map {
            GeneralParseTreeNodeModel(
                it.pushIndex,
                it.popIndex,
                it.parentPushIndex,
                it.ruleContextModel,
                it.terminalNodeModel,
            )
        }
    }

    fun getRuleClassNames(): Map<Int, String> {
        return ruleClassNames
    }

    /**
     * ErrorNode is a simple alias for TerminalNode, so we can reuse this
     */
    private fun pushTerminalNode(node: TerminalNode, isError: Boolean) {
        val model = TerminalNodeModel.fromTerminalNode(
            node,
            isError = isError,
            documentLocusHelper = documentLocusHelper,
        )
        val parent = if (nodeStack.isEmpty()) {
            null
        } else {
            nodeStack.peek()
        }

        val mutableNode = MutableNode(
            pushedNodeList.size,
            popIndex = poppedCounter,
            parentPushIndex = parent?.pushIndex,
            terminalNodeModel = model,
        )
        nodeStack.push(mutableNode)
        pushedNodeList.add(mutableNode)

        // Immediately pop
        nodeStack.pop()
        poppedCounter++
    }

    override fun visitTerminal(node: TerminalNode) {
        pushTerminalNode(node, isError = false)
    }

    override fun visitErrorNode(node: ErrorNode) {
        pushTerminalNode(node, isError = true)
    }

    override fun enterEveryRule(ctx: ParserRuleContext) {
        val parent = if (nodeStack.isEmpty()) {
            null
        } else {
            nodeStack.peek()
        }

        val mutableNode = MutableNode(
            pushedNodeList.size,
            popIndex = null,
            parentPushIndex = parent?.pushIndex,
            ruleContextModel = RuleContextModel.ofRuleContext(ctx, documentLocusHelper),
        )
        ruleClassNames[ctx.ruleIndex] = ctx::class.java.name
        nodeStack.push(mutableNode)
        pushedNodeList.add(mutableNode)
    }

    override fun exitEveryRule(ctx: ParserRuleContext) {
        val popped = nodeStack.pop()
        popped.popIndex = poppedCounter
        poppedCounter++
    }

    private class MutableNode(
        val pushIndex: Int,
        var popIndex: Int? = null,
        val parentPushIndex: Int? = null,
        var ruleContextModel: RuleContextModel? = null,
        var terminalNodeModel: TerminalNodeModel? = null,
    )
}