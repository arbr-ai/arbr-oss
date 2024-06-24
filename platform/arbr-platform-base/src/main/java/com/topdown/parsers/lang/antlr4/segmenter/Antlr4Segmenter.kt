package com.topdown.parsers.lang.antlr4.segmenter

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.lang.antlr4.base.ANTLRv4Lexer
import com.topdown.parsers.lang.antlr4.base.ANTLRv4Parser
import com.topdown.parsers.lang.universal.tree_parsing.Antlr4TreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.TreeParsingListener
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.tree.ParseTree

class Antlr4Segmenter: BaseLanguageFactory {
    override fun lexerFromFileContents(fileContents: String): Lexer {
        return ANTLRv4Lexer(CharStreams.fromString(fileContents))
            .also { it.removeErrorListeners() }
    }

    override fun makeParseTree(commonTokenStream: CommonTokenStream, errorListener: ANTLRErrorListener): ParseTree {
        return ANTLRv4Parser(commonTokenStream)
            .also { it.removeErrorListeners() }
            .also { it.addErrorListener(errorListener) }
            .grammarSpec()
    }

    override fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): TreeParsingListener {
        return Antlr4TreeParsingListener(contentLength)
    }
}
