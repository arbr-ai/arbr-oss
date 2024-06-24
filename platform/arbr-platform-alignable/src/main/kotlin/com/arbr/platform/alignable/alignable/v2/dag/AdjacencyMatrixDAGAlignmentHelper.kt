package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.alignable.util.Dijkstra
import com.arbr.platform.ml.optimization.base.ParameterValue
import com.arbr.platform.ml.optimization.base.ParameterValueProvider
import com.arbr.platform.alignable.alignable.*
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Single-use helper for AdjacencyMatrixDAGValued alignment.
 */
class AdjacencyMatrixDAGAlignmentHelper<Q : Any, O : MetricAlignable<O, O2>, O2 : Any>(
    private val target: AdjacencyMatrixDAGValued<O>,
    private val source: AdjacencyMatrixDAGValued<O>,
    private val stateInducer: ConstructiveStateInducer<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>,
    private val alignmentCache: AlignmentCache<O, O2>,
    private val parameterValueProvider: ParameterValueProvider,
) : Dijkstra<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>>() {
    private val stateMachine = ConstructiveStateMachine(stateInducer)

    private val targetMatrix = target.matrix
    private val targetValues = target.values

    private val sourceMatrix = source.matrix
    private val sourceValues = source.values

    private fun getOperationAlignment(targetOperation: O, sourceOperation: O): MetricAlignment<O, O2>? {
        val alignment = alignmentCache.dagNodeValueAlignmentCache.computeIfAbsent(targetOperation to sourceOperation) {
            Optional.ofNullable(targetOperation.alignOrNull(sourceOperation))
        }
        return alignment.getOrNull()
    }

    private fun makeAlignableDAGNode(
        operation: O,
        parentValues: List<O>,
    ): AlignableDAGNode<O, O2, O, O2> {
        val normativeEdges: List<NormativeEdge<O, O2>> = parentValues.mapNotNull { parent ->
            // Parent -> child alignment should reflect directed cost such as conditional entropy of inference
            getOperationAlignment(parent, operation)?.let { directedAlignment ->
                NormativeEdge(
                    alignmentCache,
                    parameterValueProvider,
                    Optional.of(parent),
                    parameterValueProvider.getParameterValue(ADJ_CHILD_NODE) * directedAlignment.costMetric,
                )
            }
        }

        // Need to evaluate a root case: use norm
        val orphanEdge: List<NormativeEdge<O, O2>> = listOfNotNull(
            run {
                val operationNorm = operation.empty().align(operation).costMetric
                NormativeEdge(
                    alignmentCache,
                    parameterValueProvider,
                    Optional.empty(),
                    parameterValueProvider.getParameterValue(ADJ_ROOT_NODE) * operationNorm,
                )
            }
        )

        val parentEdges = AlignableEdgeList(alignmentCache, parameterValueProvider, normativeEdges + orphanEdge)
        return AlignableDAGNode<O, O2, O, O2>(
            alignmentCache,
            parameterValueProvider,
            operation,
            parentEdges,
        )
    }

    private fun getOuterCost(
        operationKind: AdjacencyMatrixDAGAlignmentKind,
    ): ParameterValue {
        val costMetricKind = when (operationKind) {
            AdjacencyMatrixDAGAlignmentKind.MERGE -> ADJ_MERGE
            AdjacencyMatrixDAGAlignmentKind.INDUCE -> ADJ_INDUCE
            AdjacencyMatrixDAGAlignmentKind.DEDUCE -> ADJ_DEDUCE
        }
        return parameterValueProvider.getParameterValue(costMetricKind)
    }

    private fun getInducedAlignment(
        node: AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>,
        di: Int,
        targetParents: List<Int>,
    ): List<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>> {
        val nextTargetState = node.targetState
            .toMutableSet()
            .also {
                it.add(di)
            }
        val nextTargetStateCode = node.targetStateCode * 31 + di + 1

        val targetOperation = targetValues[di]
        val targetParentValues = targetParents.map { targetValues[it] }
        val targetDAGNode = makeAlignableDAGNode(targetOperation, targetParentValues)

        val kind = AdjacencyMatrixDAGAlignmentKind.INDUCE
        return stateMachine.inducibleEdits(node, targetDAGNode)
            .map { (innerAlignment, nextTraversalState) ->
                val operation = AdjacencyMatrixDAGAlignmentOperation(
                    kind,
                    innerAlignment.costMetric * getOuterCost(kind),
                    innerAlignment.targetElement,
                    innerAlignment.sourceElement,
                )

                AdjacencyMatrixDAGAlignmentTraversalState(
                    targetState = nextTargetState,
                    targetStateCode = nextTargetStateCode,
                    targetIsComplete = nextTargetState.size == targetValues.size,
                    sourceState = nextTraversalState.sourceState,
                    sourceStateCode = nextTraversalState.sourceStateCode,
                    sourceIsComplete = nextTraversalState.sourceIsComplete,
                    constructionStateCode = nextTraversalState.constructionStateCode,
                    constructionState = nextTraversalState.constructionState,
                    latestOperation = operation,
                )
            }
    }

    private fun getDeducedAlignment(
        node: AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>,
        dj: Int,
        sourceParents: List<Int>,
    ): List<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>> {
        val nextSourceState = node.sourceState
            .toMutableSet()
            .also {
                it.add(dj)
            }
        val nextSourceStateCode = node.sourceStateCode * 31 + dj + 1

        val sourceOperation = sourceValues[dj]
        val sourceParentValues = sourceParents.map { sourceValues[it] }
        val sourceDAGNode = makeAlignableDAGNode(sourceOperation, sourceParentValues)

        val kind = AdjacencyMatrixDAGAlignmentKind.DEDUCE

        return stateMachine.deducibleEdits(node, sourceDAGNode)
            .map { (innerAlignment, nextTraversalState) ->
                val operation = AdjacencyMatrixDAGAlignmentOperation(
                    kind,
                    innerAlignment.costMetric * getOuterCost(kind),
                    innerAlignment.targetElement,
                    innerAlignment.sourceElement,
                )

                AdjacencyMatrixDAGAlignmentTraversalState(
                    targetState = node.targetState,
                    targetStateCode = node.targetStateCode,
                    targetIsComplete = nextTraversalState.targetIsComplete,
                    sourceState = nextSourceState,
                    sourceStateCode = nextSourceStateCode,
                    sourceIsComplete = nextSourceState.size == sourceValues.size,
                    constructionStateCode = nextTraversalState.constructionStateCode,
                    constructionState = nextTraversalState.constructionState,
                    latestOperation = operation,
                )
            }
    }

    private fun getMergeAlignment(
        node: AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>,
        di: Int,
        targetParents: List<Int>,
        dj: Int,
        sourceParents: List<Int>,
    ): List<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>> {
        val nextTargetState = node.targetState
            .toMutableSet()
            .also {
                it.add(di)
            }
        val nextTargetStateCode = node.targetStateCode * 31 + di + 1
        val nextSourceState = node.sourceState
            .toMutableSet()
            .also {
                it.add(dj)
            }
        val nextSourceStateCode = node.sourceStateCode * 31 + dj + 1

        val targetOperation = targetValues[di]
        val sourceOperation = sourceValues[dj]

        val targetParentValues = targetParents.map { targetValues[it] }
        val targetDAGNode = makeAlignableDAGNode(targetOperation, targetParentValues)
        val sourceParentValues = sourceParents.map { sourceValues[it] }
        val sourceDAGNode = makeAlignableDAGNode(sourceOperation, sourceParentValues)

        val kind = AdjacencyMatrixDAGAlignmentKind.MERGE
        return stateMachine.viableMerges(
            node,
            targetDAGNode,
            sourceDAGNode,
        )
            .map { (innerAlignment, nextTraversalState) ->
                val operation = AdjacencyMatrixDAGAlignmentOperation(
                    kind,
                    innerAlignment.costMetric * getOuterCost(kind),
                    innerAlignment.targetElement,
                    innerAlignment.sourceElement,
                )

                AdjacencyMatrixDAGAlignmentTraversalState(
                    targetState = nextTargetState,
                    targetStateCode = nextTargetStateCode,
                    targetIsComplete = nextTargetState.size == targetValues.size,
                    sourceState = nextSourceState,
                    sourceStateCode = nextSourceStateCode,
                    sourceIsComplete = nextSourceState.size == sourceValues.size,
                    constructionStateCode = nextTraversalState.constructionStateCode,
                    constructionState = nextTraversalState.constructionState,
                    latestOperation = operation,
                )
            }
    }

    private fun getPairAlignmentOptions(
        node: AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>,
        targetTraversal: MultiParentNodeTraversal,
        sourceTraversal: MultiParentNodeTraversal,
    ): List<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>> {
        val di = targetTraversal.child
        val targetParents = targetTraversal.parents

        val dj = sourceTraversal.child
        val sourceParents = sourceTraversal.parents

        // Traditional edit distance makes an important assumption that internal decisions in a child state are
        // independent of the inbound parent state - this doesn't work as well with DAG alignment when edges represent
        // dependencies, because in a typical dependency arrangement of code, children depend on parents and vice versa.
        // A more representative implementation might use a prefix and postfix step for propagating parent information
        // to children and then back to parents.
        // At that point it might become necessary to fragment the dynamic programming state to reduce the size of the
        // state space
        return if (di == null && dj == null) {
            throw IllegalStateException()
        } else if (di == null) {
            getDeducedAlignment(node, dj!!, sourceParents)
        } else if (dj == null) {
            getInducedAlignment(node, di, targetParents)
        } else {
            getMergeAlignment(node, di, targetParents, dj, sourceParents)
        }
    }

    private fun getTransitions(
        node: AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>
    ): List<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>> {
        val targetState = node.targetState
        val sourceState = node.sourceState

        // Compute outbound unvisited neighbors
        val targetNeighbors = getViableTraversals(
            targetMatrix,
            targetState
        )
        val sourceNeighbors = getViableTraversals(
            sourceMatrix,
            sourceState
        )

        // Represent each (i, j) transition pair as well as each (i, null) and (null, j)
        val candidateTransitions = mutableListOf<Pair<MultiParentNodeTraversal, MultiParentNodeTraversal>>()
        targetNeighbors.forEach { i ->
            candidateTransitions.add(i to MultiParentNodeTraversal(emptyList(), null))
            sourceNeighbors.forEach { j ->
                candidateTransitions.add(i to j)
            }
        }
        sourceNeighbors.forEach { j ->
            candidateTransitions.add(MultiParentNodeTraversal(emptyList(), null) to j)
        }

        val allCandidateOperations: List<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>> =
            candidateTransitions
                .flatMap { getPairAlignmentOptions(node, it.first, it.second) }

        return allCandidateOperations
    }

    override val start: AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>> =
        stateMachine.initialState()

    override fun neighbors(node: AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>): List<Pair<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>, Double>> {
        val transitions = getTransitions(node)

//        transitions = transitions.filter { it.latestOperation == null || it.latestOperation.cost <= 10.0 }

//        val epsilon = 0.5
//        if (transitions.any { it.latestOperation != null && it.latestOperation.cost < epsilon }) {
//            transitions = transitions.filter { it.latestOperation != null && it.latestOperation.cost < epsilon }
//        }

//        val costs = transitions.map { it.latestOperation?.cost?.let { d -> String.format("%.02f", d) } ?: "u" }
//        println(costs.sorted())

        // Deduplicate transitions to the same state by taking the one with least cost
        val deduplicatedTransitions = transitions
            .groupBy {
                it.hashCode()
            }
            .mapValues { (_, traversals) ->
                traversals.minBy {
                    it.latestOperation?.cost?.value ?: 0.0
                }
            }
            .values
            .toList()

        return deduplicatedTransitions.map { adjacencyMatrixDagAlignmentTraversal ->
            val cost = adjacencyMatrixDagAlignmentTraversal
                .latestOperation
                ?.cost
                ?.value
                ?: 0.0

            adjacencyMatrixDagAlignmentTraversal to cost
        }
    }

    fun alignmentPath(): Pair<List<AdjacencyMatrixDAGAlignmentTraversalState<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>>, Double>? {
//        val targetStateFinalValue = (1 shl (targetValues.size)) - 1
//        val sourceStateFinalValue = (1 shl (sourceValues.size)) - 1
        return shortestPath { node ->
            node.targetState.size == targetValues.size && node.sourceState.size == sourceValues.size
        }
    }

    fun cancel() {
        // TODO
    }

    companion object {
        private data class MultiParentNodeTraversal(
            val parents: List<Int>,
            val child: Int?,
        )

        /**
         * Get viable traversals via the adjacency matrix and state bit vector
         * Interprets edges as requirements, i.e. all ancestors of a node must be visited prior to traversal
         */
        private fun getViableTraversals(
            adjacencyMatrix: List<List<Boolean>>,
            stateSet: Set<Int>
        ): List<MultiParentNodeTraversal> {
            if (adjacencyMatrix.isEmpty()) {
                return emptyList()
            }

            // Look for hollow columns
            val numColumns = adjacencyMatrix[0].size

            return (0 until numColumns).filter { j ->
                // Not yet visited
                (j !in stateSet) && adjacencyMatrix.indices.all { i ->
                    // Root or satisfied
                    !adjacencyMatrix[i][j] || (i in stateSet)
                }
            }
                .map { j ->
                    val parents = adjacencyMatrix.indices.filter { i ->
                        adjacencyMatrix[i][j]
                    }
                    MultiParentNodeTraversal(parents, j)
                }
        }
    }
}
