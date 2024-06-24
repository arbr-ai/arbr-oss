package com.arbr.engine.services.differential_content.formatter

import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.fasterxml.jackson.databind.ObjectMapper
import com.topdown.parsers.lang.json_lenient.base.JsonLenientLexer
import com.topdown.parsers.lang.json_lenient.base.JsonLenientParser
import com.topdown.parsers.lang.universal.tree_parsing.JsonLenientTreeParsingListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JsonLenientPostProcessor(
    private val mapper: ObjectMapper,
): DocumentProcessor<DiffLiteralSourceDocument> {
    private val writer = mapper.writer().withDefaultPrettyPrinter()

    private fun <V> mapTree(
        contents: String,
        mapNode: (TerminalNode) -> V,
        reduceLayer: (ParserRuleContext, List<V>?) -> V,
    ): V? {
        val lexer = JsonLenientLexer(CharStreams.fromString(contents))
        val tokens = CommonTokenStream(lexer)

        val jsonContext = JsonLenientParser(tokens).json()
        val walker = ParseTreeWalker()
        val treeListener = JsonLenientTreeParsingListener(contents.length.toLong())

        val mappedChildStack = mutableListOf<MutableList<V>>()
        var currentLayer: MutableList<V>? = null
        var lastPop: V? = null

        val listener = object : ParseTreeListener {
            override fun visitTerminal(node: TerminalNode) {
                currentLayer?.add(mapNode(node))
            }

            override fun visitErrorNode(node: ErrorNode) {
                //
            }

            override fun enterEveryRule(ctx: ParserRuleContext) {
                currentLayer?.let { mappedChildStack.add(it) }
                currentLayer = null

                val newLayer = mutableListOf<V>()
                currentLayer = newLayer
            }

            override fun exitEveryRule(ctx: ParserRuleContext) {
                val reduced = reduceLayer(ctx, currentLayer)
                val parentLayer = mappedChildStack.removeLastOrNull() ?: mutableListOf()
                currentLayer = parentLayer

                parentLayer.add(reduced)
                lastPop = reduced
            }
        }
        treeListener.listeners.add(listener)

        walker.walk(treeListener, jsonContext)
        return lastPop
    }

    private fun lintJson(input: String): String? {
        val mapped =  mapTree<String?>(input,
            { terminalNode ->
                if (
                    terminalNode.symbol.type in listOf(
                        JsonLenientParser.Comma,
                        JsonLenientParser.OpenBrace,
                        JsonLenientParser.CloseBrace,
                        JsonLenientParser.OpenBracket,
                        JsonLenientParser.CloseBracket,
                    )
                ) {
                    null
                } else {
                    terminalNode.text
                }
            },
            { parentCtx, mappedChildrenNullable ->
                val mappedChildren = mappedChildrenNullable?.filterNotNull() ?: emptyList()

                when (parentCtx) {
                    is JsonLenientParser.ObjContext -> "{\n${
                        mappedChildren.joinToString(
                            ",\n"
                        )
                    }\n}"
                    is JsonLenientParser.ArrContext -> "[\n${
                        mappedChildren.joinToString(
                            ",\n"
                        )
                    }\n]"

                    else -> mappedChildren.joinToString(" ")
                }
            }
        )

        return mapped?.let {
            val tree = mapper.readTree(it)
            writer.writeValueAsString(tree)
        }
    }

    override fun process(documentType: DiffLiteralSourceDocument): DiffLiteralSourceDocument {
        val input = documentType.text
        val output = lintJson(input) ?: run {
            logger.error("Failed to lint JSON in post-processing")
            input
        }
        return DiffLiteralSourceDocument(output)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JsonLenientPostProcessor::class.java)
    }
}