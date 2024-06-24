package com.topdown.parsers.common

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.base.CustomErrorListener
import com.topdown.parsers.base.TreeParseResults
import com.topdown.parsers.lang.universal.tree_parsing.BaseTreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.UniversalTreeParsingListener
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker

class LanguageTreeParser(
    private val baseLanguageFactory: BaseLanguageFactory,
) {

    private fun makeUniversalTreeParsingListener(contentLength: Long): BaseTreeParsingListener {
        return UniversalTreeParsingListener(contentLength)
    }

    private fun makeErrorListener(
        fileContents: String,
    ): CustomErrorListener = CustomErrorListener(fileContents)

    /**
     * Generate a representation of the file as a tree
     */
    fun treeParseFileContents(fileContents: String): TreeParseResults {
        val errorListener = makeErrorListener(fileContents)
        val lexer = baseLanguageFactory.lexerFromFileContents(fileContents)
        lexer.addErrorListener(errorListener)

        val tokens = CommonTokenStream(lexer)
        val tree: ParseTree = baseLanguageFactory.makeParseTree(tokens, errorListener)

        val walker = ParseTreeWalker()
        val treeParsingListener = baseLanguageFactory.makeTreeParsingListener(
            contentLength = fileContents.length.toLong(),
            tokens = tokens,
        )

        walker.walk(treeParsingListener, tree)
        return TreeParseResults(
            treeParsingListener.tree,
            errorListener.getSyntaxErrorInfo(),
            baseLanguageFactory.channelTokens(fileContents),
        )
    }

    /**
     * Generate a representation of the file as a tree using all rules and programmatic naming
     */
    fun treeParseFileContentsUniversal(fileContents: String): TreeParseResults {
        val errorListener = makeErrorListener(fileContents)
        val lexer = baseLanguageFactory.lexerFromFileContents(fileContents)
        lexer.addErrorListener(errorListener)

        val tokens = CommonTokenStream(lexer)
        val tree: ParseTree = baseLanguageFactory.makeParseTree(tokens, errorListener)

        val walker = ParseTreeWalker()
        val treeParsingListener = makeUniversalTreeParsingListener(
            contentLength = fileContents.length.toLong(),
        )

        walker.walk(treeParsingListener, tree)
        return TreeParseResults(
            treeParsingListener.tree,
            errorListener.getSyntaxErrorInfo(),
            baseLanguageFactory.channelTokens(fileContents),
        )
    }
}