package com.topdown.parsers.lang.universal.tree_parsing

import com.topdown.parsers.lang.css3.base.css3Parser
import com.topdown.parsers.lang.html.base.HTMLParser
import com.topdown.parsers.lang.json.base.JSONParser
import com.topdown.parsers.lang.json_lenient.base.JsonLenientParser
import com.topdown.parsers.lang.jsx.base.JavaScriptParser
import com.topdown.parsers.lang.universal.tree_parsing.util.ParserRuleContexts
import org.antlr.v4.runtime.ParserRuleContext

object GlobalTreeParsingTerminalRules {
    /**
     * JSON rules
     */
    val jsonRules = listOf(
        TreeParsingTerminalRule(
            JSONParser.JsonContext::class.java,
            "json",
        ) {
            "node"
        },
        TreeParsingTerminalRule(
            JSONParser.ObjContext::class.java,
            "obj",
        ) {
            "obj"
        },
        TreeParsingTerminalRule(
            JSONParser.ArrContext::class.java,
            "arr",
        ) {
            "arr"
        },
        TreeParsingTerminalRule(
            JSONParser.PairContext::class.java,
            "pair",
        ) {
            "pair"
        },
    )

    /**
     * JSON-Lenient rules
     */
    val jsonLenientRules = listOf(
        TreeParsingTerminalRule(
            JsonLenientParser.JsonContext::class.java,
            "json",
        ) {
            "node"
        },
        TreeParsingTerminalRule(
            JsonLenientParser.ObjContext::class.java,
            "obj",
        ) {
            "obj"
        },
        TreeParsingTerminalRule(
            JsonLenientParser.ArrContext::class.java,
            "arr",
        ) {
            "arr"
        },
        TreeParsingTerminalRule(
            JsonLenientParser.PairContext::class.java,
            "pair",
        ) {
            "pair"
        },
    )

    /**
     * CSS rules
     */
    val cssRules = listOf(
        TreeParsingTerminalRule(
            css3Parser.NestedStatementContext::class.java,
            "block",
        ) { ctx ->
            val braceIndex = ctx.text.indexOf("{")
            if (braceIndex == -1) {
                null
            } else {
                ctx.text.take(braceIndex).trim()
            }
        },
    )

    /**
     * HTML rules
     */
    val htmlRules = listOf(
        TreeParsingTerminalRule(
            HTMLParser.HtmlElementContext::class.java,
            "element",
        ) { ctx -> ctx.TAG_NAME().firstOrNull()?.text },
    )

    val javaScriptRules = listOf(
        TreeParsingTerminalRule(
            JavaScriptParser.ClassDeclarationContext::class.java,
            "class",
        ) { ctx -> ctx.identifier().text },
        TreeParsingTerminalRule(
            JavaScriptParser.FunctionDeclarationContext::class.java,
            "function",
        ) { ctx -> ctx.identifier().text },
        TreeParsingTerminalRule(
            JavaScriptParser.VariableStatementContext::class.java,
            "variable",
        ) { ctx ->
            val identifiers =
                ParserRuleContexts.findFirstChildrenOfType(ctx, JavaScriptParser.AssignableContext::class.java)
                    .flatMap {
                        ParserRuleContexts.findFirstChildrenOfType(it, JavaScriptParser.IdentifierContext::class.java)
                    }

            ParserRuleContexts.joinIdentifiers(identifiers)
        },
        TreeParsingTerminalRule(
            JavaScriptParser.ImportStatementContext::class.java,
            "import",
        ) { ctx ->
            var identifiers: List<ParserRuleContext> =
                ParserRuleContexts.findFirstChildrenOfType(ctx, JavaScriptParser.IdentifierContext::class.java)
            if (identifiers.isEmpty()) {
                identifiers =
                    ParserRuleContexts.findFirstChildrenOfType(ctx, JavaScriptParser.ImportFromBlockContext::class.java)
            }

            ParserRuleContexts.joinIdentifiers(identifiers)
        },
        TreeParsingTerminalRule(
            JavaScriptParser.HtmlElementExpressionContext::class.java,
            "html_element",
        ) {
            // No clear way to identify a literal component - will need to be disambiguated by index.
            "react_component"
        },
        TreeParsingTerminalRule(
            JavaScriptParser.ExportDefaultDeclarationContext::class.java,
            "export_default",
        ) { ctx ->
            val identifiers =
                ParserRuleContexts.findFirstChildrenOfType(ctx, JavaScriptParser.IdentifierContext::class.java)
            ParserRuleContexts.joinIdentifiers(identifiers)
        },
        TreeParsingTerminalRule(
            JavaScriptParser.ExportDeclarationContext::class.java,
            "export",
        ) { ctx ->
            val identifiers =
                ParserRuleContexts.findFirstChildrenOfType(ctx, JavaScriptParser.IdentifierContext::class.java)
            ParserRuleContexts.joinIdentifiers(identifiers)
        },
    )

    val allRules = listOf(
        cssRules,
        htmlRules,
        javaScriptRules,
    ).flatten()
}
