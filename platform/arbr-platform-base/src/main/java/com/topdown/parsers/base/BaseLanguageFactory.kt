package com.topdown.parsers.base

import com.topdown.parsers.lang.universal.tree_parsing.BaseTreeParsingListener
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTree

interface BaseLanguageFactory {
    fun lexerFromFileContents(fileContents: String): Lexer

    fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): BaseTreeParsingListener

    fun makeParseTree(
        commonTokenStream: CommonTokenStream,
        errorListener: ANTLRErrorListener,
    ): ParseTree

    fun channelTokens(fileContents: String): Map<Int, List<Token>>? {
        return null
    }
}
