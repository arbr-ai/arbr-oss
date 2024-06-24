package com.arbr.content_formats.format

import com.arbr.content_formats.format.tokenizer.TokenFormatter
import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import com.arbr.platform.data_structures_common.partial_order.PartialOrder
import com.arbr.platform.data_structures_common.partial_order.PartialOrderFlatteningScheme

class DiffLiteralPatchSectionSerializer: TokenizationSerializer<DiffLiteralPatch, DiffParsedPatchSection> {

    override fun serializeWith(
        tokens: PartialOrder<DiffParsedPatchSection>,
        formatter: TokenFormatter<DiffParsedPatchSection>
    ): DiffLiteralPatch {
        val sections = tokens.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST)
        val text = StringBuilder().run {
            var sectionEffectiveIndex = 0
            for (unformattedSection in sections) {
                if (unformattedSection.operations.isEmpty()) {
                    continue
                }
                val section = formatter.formatToken(sectionEffectiveIndex, unformattedSection)

                if (section.lineStart != null && section.lineEnd != null && section.targetLineStart != null && section.targetLineEnd != null) {
                    val sourceRangeSize = section.lineEnd - section.lineStart
                    val targetRangeSize = section.targetLineEnd - section.targetLineStart

                    // Re-introduce 1-indexing
                    val sectionHeader = "@@ -${section.lineStart + 1},${sourceRangeSize} +${section.targetLineStart + 1},$targetRangeSize @@\n"
                    append(sectionHeader)
                }

                for (operation in section.operations) {
                    val lineStart = when (operation.kind) {
                        DiffOperationKind.NUL,
                        DiffOperationKind.NOP -> " "
                        DiffOperationKind.ADD -> "+"
                        DiffOperationKind.DEL -> "-"
                    }
                    append(lineStart + operation.lineContent + "\n")
                }

                sectionEffectiveIndex++
            }
            toString()
        }

        return DiffLiteralPatch(text.trimEnd())
    }
}