package com.topdown.parsers.lang.css3.segmenter

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.lang.css3.base.css3Lexer
import com.topdown.parsers.lang.css3.base.css3Parser
import com.topdown.parsers.lang.universal.tree_parsing.CssTreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.TreeParsingListener
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.tree.ParseTree

class CssSegmenter: BaseLanguageFactory {
    override fun lexerFromFileContents(fileContents: String): Lexer {
        return css3Lexer(CharStreams.fromString(fileContents))
            .also { it.removeErrorListeners() }
    }

    override fun makeParseTree(commonTokenStream: CommonTokenStream, errorListener: ANTLRErrorListener): ParseTree {
        return css3Parser(commonTokenStream)
            .also { it.removeErrorListeners() }
            .also { it.addErrorListener(errorListener) }
            .stylesheet()
    }

    override fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): TreeParsingListener {
        return CssTreeParsingListener(contentLength)
    }
}
