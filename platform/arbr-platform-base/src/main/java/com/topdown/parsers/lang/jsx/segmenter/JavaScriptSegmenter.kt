package com.topdown.parsers.lang.jsx.segmenter

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.lang.jsx.base.JavaScriptLexer
import com.topdown.parsers.lang.jsx.base.JavaScriptParser
import com.topdown.parsers.lang.universal.tree_parsing.JavaScriptTreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.TreeParsingListener
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.tree.ParseTree

class JavaScriptSegmenter : BaseLanguageFactory {
    override fun lexerFromFileContents(fileContents: String): Lexer {
        return JavaScriptLexer(CharStreams.fromString(fileContents))
            .also { it.removeErrorListeners() }
    }

    override fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): TreeParsingListener {
        return JavaScriptTreeParsingListener(contentLength, tokens)
    }

    override fun makeParseTree(commonTokenStream: CommonTokenStream, errorListener: ANTLRErrorListener): ParseTree {
        return JavaScriptParser(commonTokenStream)
            .also { it.removeErrorListeners() }
            .also { it.addErrorListener(errorListener) }
            .program()
    }
}
