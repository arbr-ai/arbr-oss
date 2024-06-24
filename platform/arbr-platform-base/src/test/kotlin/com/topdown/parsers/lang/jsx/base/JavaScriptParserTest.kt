package com.topdown.parsers.lang.jsx.base

import com.topdown.parsers.common.GeneralLanguageTreeParser
import com.topdown.parsers.common.model.GeneralTreeParseResultModel
import com.topdown.parsers.lang.antlr4.segmenter.Antlr4Segmenter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class JavaScriptParserTest {

    private fun segmentFileContentGeneral(
        fileContent: String,
    ): GeneralTreeParseResultModel {
        val segmenter = Antlr4Segmenter()
        val generalParser = GeneralLanguageTreeParser(segmenter)

        Assertions.assertTrue(fileContent.isNotBlank())
        Assertions.assertTrue(fileContent.length < 100_000)

        return generalParser.parseText(fileContent)
    }

    @Test
    fun generate() {
        val parseTree = segmentFileContentGeneral(
            File("src/main/antlr/jsx/JavaScriptLexer.g4").readText()
        )

        println(parseTree.ruleClassNames.entries.sortedBy { it.key }.joinToString("\n") { "${it.key}. ${it.value}" })
        println()

        val loader: ClassLoader = this::class.java.classLoader

        parseTree.nodeList.forEach { node ->
            val ruleName = node.ruleContextModel?.ruleIndex?.let {
                loader.loadClass(parseTree.ruleClassNames[it]!!).simpleName
            }?.let { "Rule[$it]" }
            val tokenText = node.terminalNodeModel?.tokenModel?.text?.let { "Token[$it]" }
            val text = (ruleName ?: tokenText)!!
            val pair = "(${node.pushIndex}, ${node.popIndex})"

            println("$pair $text")
        }
    }

}
