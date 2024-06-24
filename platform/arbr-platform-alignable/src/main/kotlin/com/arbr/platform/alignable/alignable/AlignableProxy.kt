package com.arbr.platform.alignable.alignable

import com.arbr.platform.alignable.alignable.alignment.Alignment

data class AlignableProxy<V, E : Alignable<E, O>, O>(
    val sourceValue: V?,
    val alignableElement: E,
) : Alignable<AlignableProxy<V, E, O>, O> {
    override fun align(e: AlignableProxy<V, E, O>): Alignment<AlignableProxy<V, E, O>, O> {
        return when (val innerAlignment = alignableElement.align(e.alignableElement)) {
            is Alignment.Align -> Alignment.Align(
                innerAlignment.operations,
                innerAlignment.cost,
                this,
                e,
            )

            is Alignment.Equal -> Alignment.Equal(
                this,
                e,
            )
        }
    }

    override fun empty(): AlignableProxy<V, E, O> {
        return AlignableProxy(sourceValue, alignableElement.empty())
    }

    override fun applyAlignment(alignmentOperations: List<O>): AlignableProxy<V, E, O> {
        return if (alignmentOperations.isEmpty()) {
            this
        } else {
            AlignableProxy(
                sourceValue, // Retain hydrated source value when it exists. This means alignments on the node type
                // retain source info, versus entire insert/deletes which nullify the source
                alignableElement.applyAlignment(alignmentOperations),
            )
        }
    }
}

