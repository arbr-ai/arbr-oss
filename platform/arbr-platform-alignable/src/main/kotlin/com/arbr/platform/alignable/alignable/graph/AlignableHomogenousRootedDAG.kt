package com.arbr.platform.alignable.alignable.graph

import com.arbr.content_formats.mapper.Mappers
import com.fasterxml.jackson.annotation.JsonIgnore
import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.IdentifiableAlignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation
import kotlin.math.exp

interface AlignableHomogenousRootedDAG<E : Alignable<E, O>, O> :
    Alignable<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>, HomogenousRootedDAG<E> {
    override val nodeValue: E
    override val children: AlignableMap<
            AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
            MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
            >

    val listeners: MutableList<HomogenousRootedDAGAlignmentListener<E, O>>

    fun addListener(listener: HomogenousRootedDAGAlignmentListener<E, O>) {
        if (!listeners.any { it.uuid == listener.uuid }) {
            listeners.add(listener)
        }

        children.addListener(listener.childTypeMapListenerProxy())
        children.forEach { (childKey, chl) ->
            val subgraphListenerProxy = listener.subgraphListenerProxy(childKey)
            chl.addListener(subgraphListenerProxy)
            chl.forEach { subgraph ->
                subgraph.value.addListener(listener)
            }
        }
    }

    override fun align(e: AlignableHomogenousRootedDAG<E, O>): Alignment<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>> {
        val nodeAlignment = nodeValue.align(e.nodeValue)
        val childAlignment = children.align(e.children)

        val operations =
            nodeAlignment.operations
                .map { HomogenousRootedDAGAlignmentOperation.Node<E, O>(it) } + childAlignment.operations.map {
                HomogenousRootedDAGAlignmentOperation.ChildType(it)
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

    override fun empty(): AlignableHomogenousRootedDAG<E, O> {
        return AlignableHomogenousRootedDAGImpl(
            nodeValue.empty(),
            children.empty(),
        ).also { e ->
            listeners.forEach { e.addListener(it) }
        }
    }

    override fun applyAlignment(
        alignmentOperations: List<HomogenousRootedDAGAlignmentOperation<E, O>>
    ): AlignableHomogenousRootedDAG<E, O> {
        val compoundListener = HomogenousRootedDAGAlignmentListener.compoundListenerOf(listeners)

        compoundListener.didEnter(this)
        var nodeData = this.nodeValue
        var children = this.children
        for ((i, operation) in alignmentOperations.withIndex()) {
            when (operation) {
                is HomogenousRootedDAGAlignmentOperation.ChildType -> {
                    children = children.applyAlignment(listOf(operation.childTypeOperation))
                }

                is HomogenousRootedDAGAlignmentOperation.Node -> {
                    nodeData = nodeData.applyAlignment(listOf(operation.nodeOperation))
                        .also { compoundListener.didApplyNodeOperation(i, operation.nodeOperation, it) }
                }
            }
        }

        return AlignableHomogenousRootedDAGImpl(nodeData, children)
            .also { compoundListener.didExit(this) }
    }

    companion object {
        private val maxIdentifiableEditCost = 2.0
        private const val identifiableCostCoef = 0.5
    }
}

data class AlignableHomogenousRootedDAGImpl<E : Alignable<E, O>, O>(
    override val nodeValue: E,
    override val children: AlignableMap<
            AlignableMap<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>,
            MapAlignmentOperation<AlignableHomogenousRootedDAG<E, O>, HomogenousRootedDAGAlignmentOperation<E, O>>
            >,
) : AlignableHomogenousRootedDAG<E, O> {
    @JsonIgnore
    override val listeners: MutableList<HomogenousRootedDAGAlignmentListener<E, O>> = mutableListOf()

    override fun toString(): String {
        return yamlMapper.writeValueAsString(this)
    }

    companion object {
        private val yamlMapper = Mappers.yamlMapper
    }
}
