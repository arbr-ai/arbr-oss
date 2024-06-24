package com.topdown.parsers.lang.json.segmenter

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.lang.css3.base.css3Lexer
import com.topdown.parsers.lang.css3.base.css3Parser
import com.topdown.parsers.lang.json.base.JSONLexer
import com.topdown.parsers.lang.json.base.JSONParser
import com.topdown.parsers.lang.universal.tree_parsing.CssTreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.JsonTreeParsingListener
import com.topdown.parsers.lang.universal.tree_parsing.TreeParsingListener
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.tree.ParseTree

class JsonSegmenter: BaseLanguageFactory {
    override fun lexerFromFileContents(fileContents: String): Lexer {
        return JSONLexer(CharStreams.fromString(fileContents))
            .also { it.removeErrorListeners() }
    }

    override fun makeParseTree(commonTokenStream: CommonTokenStream, errorListener: ANTLRErrorListener): ParseTree {
        return JSONParser(commonTokenStream)
            .also { it.removeErrorListeners() }
            .also { it.addErrorListener(errorListener) }
            .json()
    }

    override fun makeTreeParsingListener(contentLength: Long, tokens: CommonTokenStream): TreeParsingListener {
        return JsonTreeParsingListener(contentLength)
    }
}
