package com.topdown.parsers.lang.universal.segmenter

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.lang.universal.tree_parsing.TreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.UniversalTreeParsingListener
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.tree.ParseTree

class UniversalSegmenter: BaseLanguageFactory {

    override fun lexerFromFileContents(fileContents: String): Lexer {
        throw NotImplementedError()
    }

    override fun makeParseTree(commonTokenStream: CommonTokenStream, errorListener: ANTLRErrorListener): ParseTree {
        throw NotImplementedError()
    }

    override fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): TreeParsingListener {
        return UniversalTreeParsingListener(contentLength)
    }
}