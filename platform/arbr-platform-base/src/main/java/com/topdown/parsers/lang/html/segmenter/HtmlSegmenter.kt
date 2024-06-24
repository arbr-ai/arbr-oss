package com.topdown.parsers.lang.html.segmenter

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.lang.html.base.HTMLLexer
import com.topdown.parsers.lang.html.base.HTMLParser
import com.topdown.parsers.lang.universal.tree_parsing.HtmlTreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.TreeParsingListener
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.tree.ParseTree

class HtmlSegmenter: BaseLanguageFactory {
    override fun lexerFromFileContents(fileContents: String): Lexer {
        return HTMLLexer(CharStreams.fromString(fileContents))
            .also { it.removeErrorListeners() }
    }

    override fun makeParseTree(commonTokenStream: CommonTokenStream, errorListener: ANTLRErrorListener): ParseTree {
        return HTMLParser(commonTokenStream)
            .also { it.removeErrorListeners() }
            .also { it.addErrorListener(errorListener) }
            .htmlDocument()
    }

    override fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): TreeParsingListener {
        return HtmlTreeParsingListener(contentLength)
    }
}
