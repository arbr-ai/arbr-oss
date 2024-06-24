package com.arbr.platform.alignable.morphism

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.collections.AlignableMap
import com.arbr.platform.alignable.alignable.graph.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

private class ObjectProxyListener<SourceType : HomogenousRootedDAG<V>, V, E : Alignable<E, O>, O>(
    private val owner: DifferentialGraphMorphism<SourceType, V, E, O>,
) : HomogenousRootedDAGAlignmentListener<AlignableProxy<V, E, O>, O>() {
    override val uuid: String = UUID.randomUUID().toString()

    val updates = mutableListOf<Mono<Void>>()

    private val dagStack = Stack<AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>>()

    private var newSourceValue: V? = null

    override fun didEnter(dag: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>) {
        val dagWithValue = if (dag.nodeValue.sourceValue == null) {
            // If this is null we should fail loudly as it's an invariant failure
            val parent = dagStack.peek()!!

            val newSubgraph = dag.flatMapAlignable { it.alignableElement }
            newSourceValue = owner.initializeSourceValue(
                parent,
                newSubgraph
            )

            // Inject new source value into DAG for the stack and pass along listeners
            AlignableHomogenousRootedDAGImpl<AlignableProxy<V, E, O>, O>(
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
        map: AlignableMap<AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>,
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
        element: AlignableMap<AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>
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
        element: AlignableMap<AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>
    ) {
        // If this is null we should fail loudly as it's an invariant failure
        val dag = dagStack.peek()!!

        updates.add(
            owner.removeChildType(dag, key, element)
        )
    }

    override fun didAddSubgraph(
        subgraph: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        key: String
    ) {
        // If this is null we should fail loudly as it's an invariant failure
        val dag = dagStack.peek()!!

        updates.add(
            owner.attachNewSourceElement(
                dag,
                childKey,
                key,
                subgraph.flatMapAlignable { it.alignableElement },
            )
        )
    }

    override fun didRemoveSubgraph(
        subgraph: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        key: String
    ) {
        // If this is null we should fail loudly as it's an invariant failure
        val dag = dagStack.peek()!!

        updates.add(
            owner.removeSourceElement(
                dag,
                childKey,
                key,
                subgraph.flatMapAlignable { it.alignableElement },
            )
        )
    }

    override fun didExitSubgraphSequence(
        map: AlignableMap<AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>,
        childKey: String
    ) {
        //
    }

    override fun didExit(dag: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>) {
        dagStack.pop()
        newSourceValue = null
    }
}

abstract class DifferentialGraphMorphism<SourceType : HomogenousRootedDAG<V>, V, E : Alignable<E, O>, O> {

    abstract fun encodeElement(sourceNodeValue: V): E

    abstract fun applyInnerMap(sourceGraph: AlignableHomogenousRootedDAG<E, O>): Mono<AlignableHomogenousRootedDAG<E, O>>

    abstract fun initializeSourceValue(
        parentGraph: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>,
        newSubgraph: AlignableHomogenousRootedDAG<E, O>,
    ): V?

    abstract fun addNewChildType(
        parentGraph: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        childMap: AlignableMap<AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>,
    ): Mono<Void>

    abstract fun removeChildType(
        parentGraph: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        childMap: AlignableMap<AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>, HomogenousRootedDAGAlignmentOperation<AlignableProxy<V, E, O>, O>>
    ): Mono<Void>

    abstract fun attachNewSourceElement(
        parentGraph: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        key: String,
        subgraphToAttach: AlignableHomogenousRootedDAG<E, O>,
    ): Mono<Void>

    abstract fun removeSourceElement(
        parentGraph: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O>,
        childKey: String,
        key: String,
        subgraphToRemove: AlignableHomogenousRootedDAG<E, O>,
    ): Mono<Void>

    abstract fun applySourceNodeOperation(
        sourceNodeValue: V?,
        operation: O,
    ): Mono<Void>

    fun apply(source: SourceType): Mono<Void> {
        val id = UUID.randomUUID().toString().takeLast(4)

        val pairedDAG: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O> = source.flatMapAlignable {
            AlignableProxy(it, encodeElement(it))
        }

        val flatSourceGraph = pairedDAG.flatMapAlignable { it.alignableElement }
        val targetGraphMono = applyInnerMap(flatSourceGraph)

        return targetGraphMono.flatMap { targetGraph ->
            val pairedTargetDAG: AlignableHomogenousRootedDAG<AlignableProxy<V, E, O>, O> =
                targetGraph.flatMapAlignable {
                    AlignableProxy(null, it)
                }

            val listener = ObjectProxyListener(this)
            logger.debug("[$id] Aligning differential graph")
            val pairedAlignment = pairedDAG
                .also { it.addListener(listener) }
                .align(
                    pairedTargetDAG
                        .also { it.addListener(listener) }
                )

            logger.debug("[$id] Applying differential graph alignment with ${pairedAlignment.operations.size} operations")
            pairedDAG.applyAlignment(pairedAlignment.operations)
            logger.debug("[$id] Applied differential graph alignment")

            /**
             * Perform updates in parallel
             * They're implicitly serialized against the proposed value stream for a given property
             */
            logger.debug("[$id] Will perform ${listener.updates.size} updates")
            Flux.fromIterable(listener.updates).flatMap { it.thenReturn(Unit) }.collectList()
                .doOnSubscribe {
                    logger.debug("[$id] Now performing ${listener.updates.size} updates")
                }
                .doOnNext {
                    logger.debug("[$id] Completed ${it.size} updates")
                }
                .then()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DifferentialGraphMorphism::class.java)
    }
}
