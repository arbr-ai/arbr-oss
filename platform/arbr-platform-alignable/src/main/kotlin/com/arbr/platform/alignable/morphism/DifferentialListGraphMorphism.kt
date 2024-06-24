package com.arbr.platform.alignable.morphism

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.collections.AlignableList
import com.arbr.platform.alignable.alignable.graph.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

private class ListObjectProxyListener<SourceType : HomogenousRootedListDAG<V>, V, E : Alignable<E, O>, O>(
    private val owner: DifferentialListGraphMorphism<SourceType, V, E, O>,
) : HomogenousRootedListDAGAlignmentListener<AlignableProxy<V, E, O>, O>() {
    override val uuid: String = UUID.randomUUID().toString()

    val updates = mutableListOf<Mono<Void>>()

    private val dagStack = Stack<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>>()

    private var newSourceValue: V? = null

    override fun didEnter(dag: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>) {
        val dagWithValue = if (dag.nodeValue.sourceValue == null) {
            // If this is null we should fail loudly as it's an invariant failure
            val parent = dagStack.peek()!!

            val newSubgraph = dag.flatMapAlignable { it.alignableElement }
            newSourceValue = owner.initializeSourceValue(
                parent,
                newSubgraph
            )

            // Inject new source value into DAG for the stack and pass along listeners
            AlignableHomogenousRootedListDAGImpl<AlignableProxy<V, E, O>, O>(
                dag.nodeValue.copy(sourceValue = newSourceValue),
                dag.children,
            ).also {
                dag.listeners.forEach { l ->
                    it.addListener(l)
                }
            }
        } else {
            dag
        }
        dagStack.push(dagWithValue)
    }

    override fun didEnterSubgraphSequence(
        map: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>,
        childKey: String
    ) {
        //
    }

    override fun didApplyNodeOperation(operationIndex: Int, operation: O, element: AlignableProxy<V, E, O>) {
        val sourceNodeValue = element.sourceValue ?: newSourceValue
        updates.add(
            owner.applySourceNodeOperation(sourceNodeValue, operation)
        )
    }

    override fun didAddChildType(
        operationIndex: Int,
        key: String,
        element: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>
    ) {
        // If this is null we should fail loudly as it's an invariant failure
        val dag = dagStack.peek()!!

        updates.add(
            owner.addNewChildType(
                dag,
                key,
                element.also {
                    dag.listeners.forEach { l ->
                        it.addListener(l.subgraphListenerProxy(key))
                    }
                }
            )
        )
    }

    override fun didRemoveChildType(
        operationIndex: Int,
        key: String,
        element: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>
    ) {
        // If this is null we should fail loudly as it's an invariant failure
        val dag = dagStack.peek()!!

        updates.add(
            owner.removeChildType(dag, key, element)
        )
    }

    override fun didAddSubgraph(
        subgraph: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        atIndex: Int,
    ) {
        // If this is null we should fail loudly as it's an invariant failure
        val dag = dagStack.peek()!!

        updates.add(
            owner.attachNewSourceElement(
                dag,
                childKey,
                atIndex,
                subgraph.flatMapAlignable { it.alignableElement },
            )
        )
    }

    override fun didRemoveSubgraph(
        subgraph: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        atIndex: Int,
    ) {
        // If this is null we should fail loudly as it's an invariant failure
        val dag = dagStack.peek()!!

        updates.add(
            owner.removeSourceElement(
                dag,
                childKey,
                atIndex,
                subgraph.flatMapAlignable { it.alignableElement },
            )
        )
    }

    override fun didExitSubgraphSequence(
        map: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>,
        childKey: String
    ) {
        //
    }

    override fun didExit(dag: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>) {
        dagStack.pop()
        newSourceValue = null
    }
}

abstract class DifferentialListGraphMorphism<SourceType : HomogenousRootedListDAG<V>, V, E : Alignable<E, O>, O> {

    abstract fun encodeElement(sourceNodeValue: V): E

    abstract fun applyInnerMap(sourceGraph: AlignableHomogenousRootedListDAG<E, O>): Mono<AlignableHomogenousRootedListDAG<E, O>>

    abstract fun initializeSourceValue(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>,
        newSubgraph: AlignableHomogenousRootedListDAG<E, O>,
    ): V?

    abstract fun addNewChildType(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        childList: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>,
    ): Mono<Void>

    abstract fun removeChildType(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        childList: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>
    ): Mono<Void>

    abstract fun attachNewSourceElement(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        atIndex: Int,
        subgraphToAttach: AlignableHomogenousRootedListDAG<E, O>,
    ): Mono<Void>

    abstract fun removeSourceElement(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        atIndex: Int,
        subgraphToRemove: AlignableHomogenousRootedListDAG<E, O>,
    ): Mono<Void>

    abstract fun applySourceNodeOperation(
        sourceNodeValue: V?,
        operation: O,
    ): Mono<Void>

    fun apply(source: SourceType): Mono<AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O>> {
        val id = UUID.randomUUID().toString().takeLast(4)

        val pairedDAG: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O> = source.flatMapAlignable {
            AlignableProxy(it, encodeElement(it))
        }

        val flatSourceGraph = pairedDAG.flatMapAlignable { it.alignableElement }
        val targetGraphMono = applyInnerMap(flatSourceGraph)

        return targetGraphMono.flatMap { targetGraph ->
            val pairedTargetDAG: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O> =
                targetGraph.flatMapAlignable {
                    AlignableProxy(null, it)
                }

            val listener = ListObjectProxyListener(this)
            logger.info("[$id] Aligning differential graph")
            val pairedAlignment = pairedDAG
                .also { it.addListener(listener) }
                .align(
                    pairedTargetDAG
                        .also { it.addListener(listener) }
                )

            logger.info("[$id] Applying differential graph alignment with ${pairedAlignment.operations.size} operations")
            val applied: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O> = pairedDAG.applyAlignment(pairedAlignment.operations)
            logger.info("[$id] Applied differential graph alignment")

            /**
             * Perform updates in parallel
             * They're implicitly serialized against the proposed value stream for a given property
             */
            logger.info("[$id] Will perform ${listener.updates.size} updates")
            Flux.fromIterable(listener.updates).flatMap { it.thenReturn(Unit) }.collectList()
                .doOnSubscribe {
                    logger.info("[$id] Now performing ${listener.updates.size} updates")
                }
                .doOnNext {
                    logger.info("[$id] Completed ${it.size} updates")
                }
                .thenReturn(applied)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DifferentialListGraphMorphism::class.java)
    }
}