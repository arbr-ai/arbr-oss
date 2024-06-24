package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.topdown.parsers.base.BaseLanguageFactory
import com.topdown.parsers.common.GeneralLanguageTreeParser
import com.topdown.parsers.common.LanguageTreeParser
import com.topdown.parsers.common.model.GeneralTreeParseResultModel
import com.topdown.parsers.lang.css3.segmenter.CssSegmenter
import com.topdown.parsers.lang.html.segmenter.HtmlSegmenter
import com.topdown.parsers.lang.jsx.segmenter.JavaScriptSegmenter
import com.topdown.parsers.lang.ts.segmenter.TypeScriptSegmenter
import com.topdown.parsers.lang.universal.tree_parsing.ParserRuleTree
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

class SegmenterService {

    private val jsxSegmenter = JavaScriptSegmenter()
    private val tsSegmenter = TypeScriptSegmenter()
    private val cssSegmenter = CssSegmenter()
    private val htmlSegmenter = HtmlSegmenter()

    fun getBaseLanguage(filePath: String): Pair<SegmentContentType, BaseLanguageFactory?> {
        // TODO: Pick code type by GitHub annotation or empirically
        return when {
            // Use JSX for JS for now
            filePath.endsWith(".jsx") -> SegmentContentType.JSX to jsxSegmenter
            filePath.endsWith(".js") -> SegmentContentType.JavaScript to jsxSegmenter
            filePath.endsWith(".tsx") -> SegmentContentType.TSX to tsSegmenter
            filePath.endsWith(".ts") -> SegmentContentType.TypeScript to tsSegmenter
            filePath.endsWith(".css") -> SegmentContentType.CSS to cssSegmenter
            filePath.endsWith(".html") -> SegmentContentType.HTML to htmlSegmenter
            else -> SegmentContentType.Plaintext to null
        }
    }

    fun segmenterIsAvailableForFile(filePath: String): Boolean {
        return getBaseLanguage(filePath).second != null
    }

    private fun preProcessSegment(segmentContent: String): String {
        val lines = segmentContent.split("\n")
        val firstLine = lines.take(1)

        val tail = lines.drop(1).joinToString("\n").trimIndent()
        return (firstLine + tail).joinToString("\n").trim()
    }

    private fun reprTree(
        fileContents: String,
        contentType: SegmentContentType,
        parserRuleTree: ParserRuleTree,
    ): SegmentIndexReprTree {
        val elementIndices = mutableMapOf<Pair<String, String?>, Int>()

        fun reprTreeRec(
            pTree: ParserRuleTree,
        ): SegmentIndexReprTree {
            val elementIndex = elementIndices.compute(pTree.ruleName to pTree.contextName) { _, v ->
                (v?.let { it + 1 }) ?: 0
            }!!

            val startIndex = max(0, pTree.interval.first)
            val endIndex = min(fileContents.length, pTree.interval.second + 1) // add 1 to make exclusive

            val segmentContent = preProcessSegment(
                fileContents.substring(
                    startIndex,
                    min(fileContents.length, endIndex),
                )
            )

            return SegmentIndexReprTree(
                contentType,
                pTree.ruleName,
                pTree.contextName,
                elementIndex,
                startIndex,
                endIndex,
                segmentContent,
                pTree.children.map { reprTreeRec(it) },
            )
        }

        return reprTreeRec(parserRuleTree)
    }

    private fun fillIntervals(
        parserRuleTree: ParserRuleTree
    ): List<ParserRuleTree> {
        if (parserRuleTree.children.isEmpty()) {
            return emptyList()
        }

        val augmentedChildren = mutableListOf<ParserRuleTree>()
        var endIndexExclusive = parserRuleTree.interval.first
        for (child in parserRuleTree.children) {
            if (child.interval.first > endIndexExclusive) {
                augmentedChildren.add(
                    ParserRuleTree(
                        "miscellaneous",
                        null,
                        endIndexExclusive to (child.interval.first - 1),
                        parserRuleTree,
                        mutableListOf()
                    )
                )
            }
            endIndexExclusive = child.interval.second + 1
            augmentedChildren.add(child)
        }

        if (endIndexExclusive < parserRuleTree.interval.second + 1) {
            augmentedChildren.add(
                ParserRuleTree(
                    "miscellaneous",
                    null,
                    endIndexExclusive to parserRuleTree.interval.second,
                    parserRuleTree,
                    mutableListOf()
                )
            )
        }

        return augmentedChildren
    }

    private fun nodeIsValid(tree: ParserRuleTree): Boolean {
        // Check node range is filled
        var i = tree.interval.first
        for (c in tree.children) {
            if ((c.interval.second + 1) < c.interval.first) {
                return false
            }

            if (c.interval.first != i) {
                return false
            }

            i = c.interval.second + 1
        }

        return tree.children.isEmpty() || i == tree.interval.second + 1
    }

    private fun segmentFileContentByScheme(
        filePath: String,
        fileContent: String,
    ): SegmenterTreeParseResult {
        val (contentType, baseLanguage) = getBaseLanguage(filePath)
        val segmenter = baseLanguage?.let {
            LanguageTreeParser(it)
        }

        val length = fileContent.length

        val defaultWholeFileSegmentTree = SegmentIndexReprTree(
            contentType = contentType,
            ruleName = "whole_file",
            name = filePath,
            elementIndex = 0,
            startIndex = 0,
            endIndex = length,
            content = fileContent,
            childElements = emptyList(),
        )

        if (segmenter == null) {
            return SegmenterTreeParseResult(defaultWholeFileSegmentTree, emptyList(), true)
        }

        // Have to special case blank content because the parser library doesn't handle it well
        if (fileContent.isBlank()) {
            return SegmenterTreeParseResult(defaultWholeFileSegmentTree, emptyList(), true)
        }

        val parseResult = try {
            val treeParseResult = segmenter.treeParseFileContentsUniversal(fileContent)
            if (treeParseResult.syntaxErrors.isNotEmpty()) {
                logger.warn("File $filePath had ${treeParseResult.syntaxErrors.size} syntax errors")
            }
            treeParseResult
        } catch (e: Exception) {
            logger.warn("Failed to segment file at $filePath with ${fileContent.length} bytes ${e::class.java.name}: ${e.message}")
            return SegmenterTreeParseResult(defaultWholeFileSegmentTree, emptyList(), false)
        }

        val parserRuleTree = parseResult.parserRuleTree ?: throw Exception("Failed to construct rule tree")

        val resultReprTree = reprTree(
            fileContent,
            contentType,
            parserRuleTree,
        )

        return SegmenterTreeParseResult(resultReprTree, parseResult.syntaxErrors, true)
    }

    fun segmentFileContentUniversal(
        filePath: String,
        fileContent: String,
    ): SegmenterTreeParseResult {
        return segmentFileContentByScheme(filePath, fileContent)
    }

    fun segmentFileContentGeneral(
        filePath: String,
        fileContent: String,
    ): GeneralTreeParseResultModel? {
        val (_, segmenter) = getBaseLanguage(filePath)
        val generalParser = segmenter?.let { GeneralLanguageTreeParser(it) }
            ?: return null

        // Have to special case blank content because the parser library doesn't handle it well
        if (fileContent.isBlank()) {
            return null
        }

        val length = fileContent.length
        if (length > 100_000) {
            logger.warn("Attempting to parse file of length $length: $filePath")
        }

        return generalParser.parseText(fileContent)
    }

    companion object {
        private val logger: Logger by lazy {
            LoggerFactory.getLogger(SegmenterService::class.java)
        }
    }
}
