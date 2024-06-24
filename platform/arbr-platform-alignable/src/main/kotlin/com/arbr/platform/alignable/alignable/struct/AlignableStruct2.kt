package com.arbr.platform.alignable.alignable.struct

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.alignment.Alignment

open class AlignableStruct2<A: AlignableStruct2<A, E1, O1, E2, O2>, E1: Alignable<E1, O1>, O1, E2: Alignable<E2, O2>, O2>(
    val value1: E1,
    val value2: E2,
): Alignable<A, Pair<O1?, O2?>> {
    override fun align(e: A): Alignment<A, Pair<O1?, O2?>> {
        @Suppress("UNCHECKED_CAST")
        this as A

        val a1 = value1.align(e.value1)
        val a1Ops: List<Pair<O1?, O2?>> = a1.operations.map { it to null }

        val a2 = value2.align(e.value2)
        val a2Ops: List<Pair<O1?, O2?>> = a2.operations.map { null to it }

        return if (a1 is Alignment.Equal && a2 is Alignment.Equal) {
            Alignment.Equal(this, e)
        } else {
            Alignment.Align(
                a1Ops + a2Ops,
                (a1.cost) + (a2.cost),
                this,
                e,
            )
        }
    }

    override fun empty(): A {
        @Suppress("UNCHECKED_CAST")
        return AlignableStruct2<A, E1, O1, E2, O2>(
            value1.empty(),
            value2.empty(),
        ) as A
    }

    override fun applyAlignment(alignmentOperations: List<Pair<O1?, O2?>>): A {
        val e1 = value1.applyAlignment(alignmentOperations.mapNotNull { it.first })
        val e2 = value2.applyAlignment(alignmentOperations.mapNotNull { it.second })
        @Suppress("UNCHECKED_CAST")
        return AlignableStruct2<A, E1, O1, E2, O2>(e1, e2) as A
    }

}
