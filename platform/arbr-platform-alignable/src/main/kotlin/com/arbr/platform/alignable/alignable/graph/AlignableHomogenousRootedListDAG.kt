package com.arbr.platform.alignable.alignable.graph

import com.arbr.content_formats.mapper.Mappers
import com.fasterxml.jackson.annotation.JsonIgnore
import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.IdentifiableAlignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.alignable.alignable.collections.AlignableList
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.alignable.alignable.collections.SequenceAlignmentOperation
import kotlin.math.exp

/**
 * TODO: Refactor alignable collections to deduplicate list- and map-backed DAGs
 */
interface AlignableHomogenousRootedListDAG<E : Alignable<E, O>, O> :
    Alignable<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
    HomogenousRootedListDAG<E> {
    override val nodeValue: E
    override val children: AlignableMap<
            AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
            SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
            >

    val listeners: MutableList<HomogenousRootedListDAGAlignmentListener<E, O>>

    fun addListener(listener: HomogenousRootedListDAGAlignmentListener<E, O>) {
        if (!listeners.any { it.uuid == listener.uuid }) {
            listeners.add(listener)
        }

        children.addListener(listener.childTypeMapListenerProxy())
        children.forEach { (childKey, chl) ->
            val subgraphListenerProxy = listener.subgraphListenerProxy(childKey)
            chl.addListener(subgraphListenerProxy)
            chl.forEach { subgraph ->
                subgraph.addListener(listener)
            }
        }
    }

    override fun align(e: AlignableHomogenousRootedListDAG<E, O>): Alignment<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>> {
        val nodeAlignment = nodeValue.align(e.nodeValue)
        val childAlignment = children.align(e.children)

        val operations =
            nodeAlignment.operations
                .map { HomogenousRootedListDAGAlignmentOperation.Node<E, O>(it) } + childAlignment.operations.map {
                HomogenousRootedListDAGAlignmentOperation.ChildType(it)
            }

        val cost = nodeAlignment.cost + childAlignment.cost

        return if (operations.isEmpty()) {
            Alignment.Equal(this, e)
        } else {
            // Small hack: if the node type is identified by a UUID, cap the cost at 2 (cost of insert + delete)
            @Suppress("DEPRECATION") val adjustedCost = if (nodeValue is IdentifiableAlignable<*, *> ||
                (
                        nodeValue is AlignableProxy<*, *, *>
                                && (nodeValue as AlignableProxy<*, *, *>).alignableElement is IdentifiableAlignable<*, *>
                        )
            ) {
                maxIdentifiableEditCost * (1 - exp(-1.0 * identifiableCostCoef * cost))
            } else {
                cost
            }

            Alignment.Align(operations, adjustedCost, this, e)
        }
    }

    override fun empty(): AlignableHomogenousRootedListDAG<E, O> {
        return AlignableHomogenousRootedListDAGImpl(
            nodeValue.empty(),
            children.empty(),
        ).also { e ->
            listeners.forEach { e.addListener(it) }
        }
    }

    override fun applyAlignment(
        alignmentOperations: List<HomogenousRootedListDAGAlignmentOperation<E, O>>
    ): AlignableHomogenousRootedListDAG<E, O> {
        val compoundListener = HomogenousRootedListDAGAlignmentListener.compoundListenerOf(listeners)

        compoundListener.didEnter(this)
        var nodeData = this.nodeValue
        var children = this.children
        for ((i, operation) in alignmentOperations.withIndex()) {
            when (operation) {
                is HomogenousRootedListDAGAlignmentOperation.ChildType -> {
                    children = children.applyAlignment(listOf(operation.childTypeOperation))
                }

                is HomogenousRootedListDAGAlignmentOperation.Node -> {
                    nodeData = nodeData.applyAlignment(listOf(operation.nodeOperation))
                        .also { compoundListener.didApplyNodeOperation(i, operation.nodeOperation, it) }
                }
            }
        }

        return AlignableHomogenousRootedListDAGImpl(nodeData, children)
            .also { compoundListener.didExit(this) }
    }

    companion object {
        private val maxIdentifiableEditCost = 2.0
        private const val identifiableCostCoef = 0.5
    }
}

data class AlignableHomogenousRootedListDAGImpl<E : Alignable<E, O>, O>(
    override val nodeValue: E,
    override val children: AlignableMap<
            AlignableList<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>,
            SequenceAlignmentOperation<AlignableHomogenousRootedListDAG<E, O>, HomogenousRootedListDAGAlignmentOperation<E, O>>
            >,
) : AlignableHomogenousRootedListDAG<E, O> {
    @JsonIgnore
    override val listeners: MutableList<HomogenousRootedListDAGAlignmentListener<E, O>> = mutableListOf()

    override fun toString(): String {
        return yamlMapper.writeValueAsString(this)
    }

    companion object {
        private val yamlMapper = Mappers.yamlMapper
    }
}
