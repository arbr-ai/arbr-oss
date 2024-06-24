package com.topdown.parsers.base

import com.topdown.parsers.common.model.RecognitionExceptionModel
import com.topdown.parsers.common.model.SyntaxErrorModel
import com.topdown.parsers.common.model.TokenModel
import com.topdown.parsers.util.DocumentLocusHelper
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

@Suppress("ControlFlowWithEmptyBody")
class CustomErrorListener(
    private val fileContents: String,
) : ANTLRErrorListener {
    private val syntaxErrorInfo = ConcurrentLinkedDeque<SyntaxErrorModel>()

    private val documentLocusHelper = DocumentLocusHelper(fileContents)

    fun getSyntaxErrorInfo(): List<SyntaxErrorModel> {
        return syntaxErrorInfo.toList()
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        val offendingToken = offendingSymbol as? Token?

        // logger.debug("Syntax error: $line $charPositionInLine $msg")
        syntaxErrorInfo.add(
            SyntaxErrorModel(
                offendingToken?.let { TokenModel.fromToken(it, documentLocusHelper) },
                line,
                charPositionInLine,
                documentLocusHelper.computeLocus(line, charPositionInLine),
                msg,
                e?.let { RecognitionExceptionModel.fromRecognitionException(it, documentLocusHelper) },
            )
        )
    }

    override fun reportAmbiguity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        exact: Boolean,
        ambigAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
        if (recognizer == null) {
            // logger.debug("Ambiguity reported in unknown recognizer")
        } else {
            // logger.debug("Ambiguity reported in {}", recognizer::class.java)
        }
    }

    override fun reportAttemptingFullContext(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        conflictingAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
        if (recognizer == null) {
            // logger.debug("Full context attempt reported in unknown recognizer")
        } else {
            // logger.debug("Full context attempt reported in {}", recognizer::class.java)
        }
    }

    override fun reportContextSensitivity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        prediction: Int,
        configs: ATNConfigSet?
    ) {
        if (recognizer == null) {
            // logger.debug("Context sensitivity reported in unknown recognizer")
        } else {
            // logger.debug("Context sensitivity reported in {}", recognizer::class.java)
        }
    }

    companion object {
        private val logger: Logger by lazy {
            LoggerFactory.getLogger(CustomErrorListener::class.java)
        }
    }
}