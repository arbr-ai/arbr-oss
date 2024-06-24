package com.topdown.parsers.common

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.base.CustomErrorListener
import com.topdown.parsers.common.model.GeneralTreeParseResultModel
import com.topdown.parsers.common.model.TokenModel
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker

/**
 * Intended to eventually replace LanguageTreeParser as a generalization
 */
class GeneralLanguageTreeParser(
    private val baseLanguageFactory: BaseLanguageFactory,
) {

    private fun makeErrorListener(
        fileContents: String,
    ): CustomErrorListener = CustomErrorListener(fileContents)

    /**
     * Generate a representation of the text as a tree
     */
    fun parseText(text: String): GeneralTreeParseResultModel {
        val errorListener = makeErrorListener(text)
        val lexer = baseLanguageFactory.lexerFromFileContents(text)
        lexer.addErrorListener(errorListener)

        val tokens = CommonTokenStream(lexer)
        val tree: ParseTree = baseLanguageFactory.makeParseTree(tokens, errorListener)

        val walker = ParseTreeWalker()
        val treeParsingListener = GeneralParseTreeListener(text)

        val channelTokens = baseLanguageFactory.channelTokens(text)
            ?.mapValues { (_, tokens) ->
                tokens.map {
                    TokenModel.fromToken(it, treeParsingListener.documentLocusHelper)
                }
        }

        walker.walk(treeParsingListener, tree)
        return GeneralTreeParseResultModel(
            treeParsingListener.getTreeModel(),
            errorListener.getSyntaxErrorInfo(),
            channelTokens,
            treeParsingListener.getRuleClassNames(),
        )
    }
}