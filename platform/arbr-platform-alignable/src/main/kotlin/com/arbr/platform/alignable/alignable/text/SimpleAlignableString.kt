package com.arbr.platform.alignable.alignable.text

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.SwapAlignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.alignable.alignable.collections.AlignableList

/**
 * Simple string alignable via Character edit sequence.
 */
@Suppress("DEPRECATION")
@Deprecated("TODO: Replace with Levenshtein-based alignment")
data class SimpleAlignableString(
    val text: String,
) : Alignable<SimpleAlignableString, SimpleStringAlignmentOperation> {
    private val charSequence = AlignableList(text.toList().map { SwapAlignable(it) })

    override fun align(e: SimpleAlignableString): Alignment<SimpleAlignableString, SimpleStringAlignmentOperation> {
        val innerAlignment = charSequence.align(e.charSequence)
        return Alignment.of(
            innerAlignment.operations.map { SimpleStringAlignmentOperation(it) },
            innerAlignment.cost,
            innerAlignment.sourceElement.elements.let { cl ->
                SimpleAlignableString(String(cl.map { it.element }.toCharArray()))
            },
            innerAlignment.targetElement.elements.let { cl ->
                SimpleAlignableString(String(cl.map { it.element }.toCharArray()))
            },
        )
    }

    override fun empty(): SimpleAlignableString {
        return SimpleAlignableString("")
    }

    override fun applyAlignment(alignmentOperations: List<SimpleStringAlignmentOperation>): SimpleAlignableString {
        val innerApplied = charSequence.applyAlignment(alignmentOperations.map { it.charSequenceOperation })
        return SimpleAlignableString(String(innerApplied.map { it.element }.toCharArray()))
    }
}
