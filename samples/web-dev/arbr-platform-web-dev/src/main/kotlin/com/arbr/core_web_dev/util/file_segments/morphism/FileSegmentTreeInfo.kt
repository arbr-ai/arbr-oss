package com.arbr.core_web_dev.util.file_segments.morphism

import com.arbr.object_model.core.resource.field.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.alignable.alignable.struct.DoubleNullable
import com.arbr.platform.alignable.alignable.struct.Struct2

typealias FileSegmentTreeInfoOperation = Struct2<
        DoubleNullable<ArbrFileSegmentRuleNameValue>,
        DoubleNullable<ArbrFileSegmentNameValue>,
        >

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FileSegmentTreeInfo(
    val elementRuleName: ArbrFileSegmentRuleNameValue?,
    val elementName: ArbrFileSegmentNameValue?,
) : Alignable<FileSegmentTreeInfo, FileSegmentTreeInfoOperation> {
    override fun align(e: FileSegmentTreeInfo): Alignment<FileSegmentTreeInfo, FileSegmentTreeInfoOperation> {
        if (this == e) {
            return Alignment.Equal(this, e)
        }

        val t1 = if (elementRuleName?.value == e.elementRuleName?.value) {
            DoubleNullable.Empty()
        } else if (e.elementRuleName == null) {
            DoubleNullable.Null()
        } else {
            DoubleNullable.Some(e.elementRuleName)
        }
        val t2 = if (elementName?.value == e.elementName?.value) {
            DoubleNullable.Empty()
        } else if (e.elementName == null) {
            DoubleNullable.Null()
        } else {
            DoubleNullable.Some(e.elementName)
        }

        if (t1 is DoubleNullable.Empty && t2 is DoubleNullable.Empty) {
            return Alignment.Equal(this, e)
        }

        val cost = (
                (if (t1 is DoubleNullable.Empty) 0.0 else 0.0)
                        + (if (t2 is DoubleNullable.Empty) 0.0 else 0.0)
                )

        return Alignment.Align(
            listOf(
                Struct2(t1, t2)
            ),
            cost,
            this,
            e,
        )
    }

    override fun applyAlignment(alignmentOperations: List<FileSegmentTreeInfoOperation>): FileSegmentTreeInfo {
        var elt = this
        for ((i, op) in alignmentOperations.withIndex()) {
            val (t1, t2) = op

            elt = elt.copy(
                elementRuleName = when (t1) {
                    is DoubleNullable.Empty -> elementRuleName
                    is DoubleNullable.Null -> null
                    is DoubleNullable.Some -> t1.value
                },
                elementName = when (t2) {
                    is DoubleNullable.Empty -> elementName
                    is DoubleNullable.Null -> null
                    is DoubleNullable.Some -> t2.value
                },
            )
        }

        return elt
    }

    override fun empty(): FileSegmentTreeInfo {
        return FileSegmentTreeInfo(null, null)
    }

    override fun equals(other: Any?): Boolean {
        return if (other != null && other is FileSegmentTreeInfo) {
            (
                    elementRuleName?.value == other.elementRuleName?.value
                            && elementName?.value == other.elementName?.value
                    )
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return listOf(
            elementRuleName?.value,
            elementName?.value,
        ).hashCode()
    }

}