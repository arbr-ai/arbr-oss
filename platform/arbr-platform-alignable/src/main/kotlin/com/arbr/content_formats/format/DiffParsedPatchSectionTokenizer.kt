package com.arbr.content_formats.format

import com.arbr.content_formats.format.tokenizer.LineTokenizer
import com.arbr.content_formats.format.tokenizer.Tokenizer
import com.arbr.platform.data_structures_common.partial_order.LinearOrderList
import kotlin.math.max

class DiffParsedPatchSectionTokenizer :
    Tokenizer<DiffLiteralPatch, LinearOrderList<DiffParsedPatchSection>, DiffParsedPatchSection> {
    private val parser = DiffOperationParser()

    private val diffLineTokenizer = LineTokenizer().mapIntoNotNull(LinearOrderList(emptyList())) { (lineNumber, line) ->
        parser
            .parse(lineNumber, line)
    }

    override fun tokenize(document: DiffLiteralPatch): LinearOrderList<DiffParsedPatchSection> {
        val allLines = diffLineTokenizer.tokenize(document.text)

        val sections = mutableListOf<DiffParsedPatchSection>()
        var sectionStartLine: Int? = null
        var sectionEndLine: Int? = null
        var sectionTargetStartLine: Int? = null
        var sectionTargetEndLine: Int? = null
        var sectionOpIndex = 0
        var sectionLines = mutableListOf<DiffOperation>()
        for (line in allLines) {
            when (line) {
                is Either.Left -> {
                    val op = line.value

                    if (sectionTargetStartLine != null) {
                        sectionLines.add(op.copy(lineNumber = sectionTargetStartLine + sectionOpIndex))
                    } else {
                        sectionLines.add(op)
                    }
                    sectionOpIndex++
                }

                is Either.Right -> {
                    when (val specialLine = line.value) {
                        DiffOperationParser.DiffPatchSpecialLine.Comment,
                        DiffOperationParser.DiffPatchSpecialLine.DiffHeader,
                        DiffOperationParser.DiffPatchSpecialLine.FileMinus,
                        DiffOperationParser.DiffPatchSpecialLine.FilePlus,
                        DiffOperationParser.DiffPatchSpecialLine.IndexHeader -> {
                            // Do nothing
                        }

                        is DiffOperationParser.DiffPatchSpecialLine.SectionRange -> {
                            if (sectionLines.isNotEmpty()) {
                                val adjustedEndLine = sectionLines.maxOfOrNull { it.lineNumber + 1 } ?: sectionEndLine
                                val section = DiffParsedPatchSection(
                                    sectionStartLine,
                                    adjustedEndLine,
                                    sectionTargetStartLine,
                                    sectionTargetEndLine,
                                    LinearOrderList(sectionLines)
                                )
                                sections.add(section)
                            }

                            val effectiveSourceLineStart = max(0, specialLine.sourceLineStart)
                            sectionStartLine = effectiveSourceLineStart
                            sectionEndLine =
                                effectiveSourceLineStart + specialLine.sourceRangeSize // Exclusive end range (not verified)
                            val effectiveTargetLineStart = max(0, specialLine.targetLineStart)
                            sectionTargetStartLine = effectiveTargetLineStart
                            sectionTargetEndLine =
                                effectiveTargetLineStart + specialLine.targetRangeSize // Exclusive end range (not verified)
                            sectionLines = mutableListOf()
                            sectionOpIndex = 0
                        }
                    }
                }
            }
        }

        if (sectionLines.isNotEmpty()) {
            val adjustedEndLine = sectionLines.maxOfOrNull { it.lineNumber + 1 } ?: sectionEndLine
            val section = DiffParsedPatchSection(
                sectionStartLine,
                adjustedEndLine,
                sectionTargetStartLine,
                sectionTargetEndLine,
                LinearOrderList(sectionLines)
            )
            sections.add(section)
        }

        return LinearOrderList(sections)
    }

}