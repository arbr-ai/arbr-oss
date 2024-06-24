package com.topdown.parsers.lang.json_lenient.segmenter

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.lang.json_lenient.base.JsonLenientLexer
import com.topdown.parsers.lang.json_lenient.base.JsonLenientParser
import com.topdown.parsers.lang.universal.tree_parsing.JsonLenientTreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.TreeParsingListener
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree

class JsonLenientSegmenter: BaseLanguageFactory {
    override fun lexerFromFileContents(fileContents: String): Lexer {
        return JsonLenientLexer(CharStreams.fromString(fileContents))
            .also { it.removeErrorListeners() }
    }

    override fun channelTokens(fileContents: String): Map<Int, List<Token>>? {
        // TODO: Just fill once then split on channel
        return null

//        return JsonLenientLexer.channelNames.mapIndexedNotNull { index, _ ->
//            val lexer = JsonLenientLexer(CharStreams.fromString(fileContents))
//            val tokens = CommonTokenStream(lexer, index)
//            tokens.fill()
//            index to tokens.tokens
//        }.toMap()
    }

    override fun makeParseTree(commonTokenStream: CommonTokenStream, errorListener: ANTLRErrorListener): ParseTree {
        return JsonLenientParser(commonTokenStream)
            .also { it.removeErrorListeners() }
            .also { it.addErrorListener(errorListener) }
            .json()
    }

    override fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): TreeParsingListener {
        return JsonLenientTreeParsingListener(contentLength)
    }
}
