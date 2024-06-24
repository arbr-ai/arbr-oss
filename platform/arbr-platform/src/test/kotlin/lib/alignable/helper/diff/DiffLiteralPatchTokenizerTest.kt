package com.arbr.alignable.helper.diff

import com.arbr.content_formats.format.*
import com.arbr.data_structures_common.partial_order.singletonPoset
import org.apache.commons.text.similarity.LevenshteinDistance
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class DiffLiteralPatchTokenizerTest {
    private val tokenizer = DiffLiteralPatchTokenizer()
    private val sectionSerializer = DiffLiteralPatchSectionSerializer()

    private val diffCss0 = File("src/test/resources/content/diff_css_llm_output_0.txt").readText()

    @Test
    fun `tokenizes blank lines`() {
        val tokens = tokenizer.tokenize(DiffLiteralPatch(diffCss0))
        println(tokens.joinToString("\n") { it.toString() })

        val numPatchLines = tokens.size
        val numAdds = 5
        assertEquals(numAdds, tokens.count { it.kind == DiffOperationKind.ADD })

        // Rest are NOPs
        assertEquals(numPatchLines - numAdds, tokens.count { it.kind == DiffOperationKind.NOP })

        val reSerialized = sectionSerializer.serialize(
            singletonPoset(
                DiffParsedPatchSection(
                    lineStart = null,
                    lineEnd = null,
                    targetLineStart = null,
                    targetLineEnd = null,
                    operations = tokens,
                )
            )
        )

        // We expect it to add back some spaces at the beginning of lines, but not to have a huge difference
        val diffSize = LevenshteinDistance.getDefaultInstance().apply(diffCss0, reSerialized.text)
        assertEquals(4, diffSize)

        val originalLines = diffCss0.split("\n")
        val reSerializedLines = diffCss0.split("\n")
        assertEquals(originalLines.size, reSerializedLines.size)

        for ((line0, line1) in originalLines.zip(reSerializedLines)) {
            if (line0.isNotBlank() && line1.isNotBlank()) {
                assertEquals(line0, line1)
            }
        }
    }

}