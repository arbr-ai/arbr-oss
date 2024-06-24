package com.arbr.platform.alignable.alignable.collections

import com.arbr.content_formats.mapper.Mappers
import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.NoViableAlignmentException
import com.arbr.platform.alignable.alignable.alignment.Alignment
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.max
import kotlin.math.min

open class AlignableList<E : Alignable<E, O>, O>(
    val elements: List<E>,
) : Alignable<AlignableList<E, O>, SequenceAlignmentOperation<E, O>>, List<E> by elements {
    private val listeners: MutableList<SequenceAlignmentListener<E, O>> = mutableListOf()

    fun addListener(listener: SequenceAlignmentListener<E, O>) {
        if (!listeners.any { it.uuid == listener.uuid }) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: SequenceAlignmentListener<E, O>) {
        listeners.remove(listener)
    }

    private data class PathStep<E: Alignable<E, O>, O>(
        val allowable: Boolean,
        val nextI: Int?,
        val nextJ: Int?,
        val stepOperations: List<SequenceAlignmentOperation<E, O>>,
        val totalCost: Double?,
    )

    override fun align(e: AlignableList<E, O>): Alignment<AlignableList<E, O>, SequenceAlignmentOperation<E, O>> {
        val id = UUID.randomUUID().toString().takeLast(4)
//        if (this.elements.size > 100 || e.elements.size > 100) {
//            logger.info("[$id] Aligning list size ${this.elements.size} against ${e.elements.size}")
//        }

        val sourceSeq = this.elements
        val targetSeq = e.elements

        val m = elements.size
        val n = e.elements.size
        val resMatrix = Array<Array<PathStep<E, O>?>>(m + 1) {
            Array<PathStep<E, O>?>(n + 1) { null }
        }
        val sourceAlignments = Array<Pair<E, Alignment<E, O>>?>(m + 1) { null }
        val targetAlignments = Array<Pair<E, Alignment<E, O>>?>(n + 1) { null }

        val initialDeviation = m - n  // + j - i
        val maximumMarginalDeviation = max(8, max(m, n) / 4)
        val deviationRange = min(-maximumMarginalDeviation, -maximumMarginalDeviation + initialDeviation)..max(maximumMarginalDeviation, maximumMarginalDeviation + initialDeviation)

        fun alignSequencesInner(
            i: Int,
            j: Int,
        ): PathStep<E, O> {
            if (i == sourceSeq.size && j == targetSeq.size) {
                return PathStep(true, null, null, emptyList(), 0.0)
            } else if (i >= sourceSeq.size && j >= targetSeq.size) {
                throw Exception()
            }

            val nInserted = j - i
            val deviation = initialDeviation + nInserted

            if (deviation !in deviationRange) {
                return PathStep(false, null, null, emptyList(), null)
            }

            val insert = lazy {
                val alignment = resMatrix[i][j + 1]!!
                if (!alignment.allowable) {
                    return@lazy alignment
                }

                val targetElement = targetSeq[j]

                val (empty, emptyEditAlignment) = targetAlignments[j] ?: run {
                    val empty = targetElement.empty()
                    (empty to empty.align(targetElement))
                        .also { targetAlignments[j] = it }
                }

                val insertOp = SequenceAlignmentOperation.Insert<E, O>(
                    i + nInserted,
                    emptyList(),
                    empty,
                    1.0,
                )
                val editOp = SequenceAlignmentOperation.Edit(
                    i + nInserted,
                    emptyEditAlignment.operations,
                    empty,
                    targetElement,
                    emptyEditAlignment.cost,
                )
                val insertOperations = listOf(
                    insertOp,
                    editOp,
                )
                PathStep(
                    true,
                    i,
                    j + 1,
                    insertOperations,
                    insertOp.cost + editOp.cost + (alignment.totalCost ?: 0.0), // Insert should always be possible
                )
            }

            val delete = lazy {
                val alignment = resMatrix[i + 1][j]!!
                if (!alignment.allowable) {
                    return@lazy alignment
                }

                val sourceElement = sourceSeq[i]

                val (empty, emptyEditAlignment) = sourceAlignments[i] ?: run {
                    val empty = sourceElement.empty()
                    (empty to empty.align(sourceElement))
                        .also { sourceAlignments[i] = it }
                }

                val editOp = SequenceAlignmentOperation.Edit(
                    i + nInserted,
                    emptyEditAlignment.operations,
                    sourceElement,
                    empty,
                    emptyEditAlignment.cost,
                )
                val deleteOp = SequenceAlignmentOperation.Delete<E, O>(
                    i + nInserted,
                    emptyList(),
                    empty,
                    1.0,
                )
                val deleteOperations = listOf(
                    editOp,
                    deleteOp,
                )
                PathStep(
                    true,
                    i + 1,
                    j,
                    deleteOperations,
                    editOp.cost + deleteOp.cost + (alignment.totalCost ?: 0.0), // Insert should always be possible
                )
            }

            if (i >= sourceSeq.size) {
                // must insert
                return insert.value
            }

            if (j >= targetSeq.size) {
                // must delete
                return delete.value
            }

            val sourceElt = sourceSeq[i]
            val targetElt = targetSeq[j]
            val editAlignment = try {
                sourceElt.align(targetElt)
            } catch (e: NoViableAlignmentException) {
                null
            }

            val possibleSequences = mutableListOf<PathStep<E, O>>()
            if (editAlignment != null) {
                val subAlignment = resMatrix[i + 1][j + 1]!!
                if (!subAlignment.allowable) {
                    return subAlignment
                }

                if (editAlignment.operations.isEmpty()) {
                    // Treat as equal, don't wrap in an edit op
                    return subAlignment
                }

                val editOperation = SequenceAlignmentOperation.Edit(
                    i + nInserted,
                    editAlignment.operations,
                    sourceElt,
                    targetElt,
                    editAlignment.cost,
                )

                possibleSequences.add(
                    PathStep(
                        true,
                        i + 1,
                        j + 1,
                        listOf(editOperation),
                        editAlignment.cost + (subAlignment.totalCost ?: 0.0),
                    )
                )
            }

            possibleSequences.add(insert.value)
            possibleSequences.add(delete.value)

            return possibleSequences
                .filter { it.allowable && it.totalCost != null }
                .sortedBy {
                    it.totalCost
                }
                .firstOrNull()
                ?: PathStep(false, null, null, emptyList(), 0.0)
        }

        val maxIJ = m + n
        for (ijSum in (0..maxIJ).reversed()) {
            for (i in 0..min(m, ijSum)) {
                val j = ijSum - i
                if (j in 0..n) {
                    val step = alignSequencesInner(i, j)
                    resMatrix[i][j] = step
                }
            }
        }

        var node = resMatrix[0][0]!!

        if (!node.allowable) {
            throw NoViableAlignmentException("No viable alignment within deviation allowance", null, null)
        }
        val totalCost = node.totalCost!!

        val path = node.stepOperations.toMutableList()
        while (node.nextI != null && node.nextJ != null) {
            node = resMatrix[node.nextI!!][node.nextJ!!]!!
            path.addAll(node.stepOperations)
        }

        val result = Alignment.of(
            path,
            totalCost,
            this,
            e,
        )
//        if (this.elements.size > 100 || e.elements.size > 100) {
//            logger.info("[$id] Finished aligning lists and got ${result.operations.size} operations")
//        }

        return result
    }

    override fun empty(): AlignableList<E, O> {
        return AlignableList<E, O>(emptyList()).also { e ->
            listeners.forEach { e.addListener(it) }
        }
    }

    /**
     * Apply an operation to immutably construct a new version of this alignable.
     */
    override fun applyAlignment(
        alignmentOperations: List<SequenceAlignmentOperation<E, O>>
    ): AlignableList<E, O> {
        val compoundListener = SequenceAlignmentListener.compoundListenerOf(listeners)
        compoundListener.didEnterSequence(this)
        val elementList = elements.toMutableList()
        for ((i, operation) in alignmentOperations.withIndex()) {
            when (operation) {
                is SequenceAlignmentOperation.Delete<E, O> -> {
                    elementList.removeAt(operation.atIndex)
                        .also { compoundListener.didRemoveElement(i, operation.atIndex, operation.element) }
                }

                is SequenceAlignmentOperation.Edit<E, O> -> {
                    elementList[operation.atIndex] =
                        elementList[operation.atIndex].applyAlignment(
                            operation.alignment
                        )
                            .also {
                                compoundListener.didEditElement(
                                    i,
                                    operation.atIndex,
                                    operation.fromElement,
                                    operation.element
                                )
                            }
                }

                is SequenceAlignmentOperation.Insert<E, O> -> {
                    elementList.add(operation.atIndex, operation.element)
                        .also { compoundListener.didAddElement(i, operation.atIndex, operation.element) }
                }
            }
        }
        return AlignableList(elementList)
            .also { compoundListener.didExitSequence(it) }
    }

    override fun toString(): String {
        return yamlMapper.writeValueAsString(elements)
    }

    companion object {
        private val yamlMapper = Mappers.yamlMapper

        private val logger = LoggerFactory.getLogger(AlignableList::class.java)
    }
}