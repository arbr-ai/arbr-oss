package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import java.util.*

class ConstructiveStateMachine<Q : Any, O : MetricAlignable<O, O2>, O2>(
    private val inducer: ConstructiveStateInducer<Q, O, O2>,
) {

    private val stateRepository = mutableMapOf<Int, Q>(
        inducer.initialStateCode to inducer.initialState
    )

    fun initialState(): AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2> {
        return AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>(
            targetState = emptySet(),
            targetStateCode = 0,
            targetIsComplete = false,
            sourceState = emptySet(),
            sourceStateCode = 0,
            sourceIsComplete = false,
            constructionStateCode = inducer.initialStateCode,
            constructionState = inducer.initialState,
        )
    }

    private fun applyStateTransition(
        node: AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>,
        operation: O,
    ): AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2> {
        val stateCode = node.constructionStateCode
        val currentState = stateRepository[stateCode] ?: throw IllegalStateException()

        val (nextState, nextStateCode) = inducer.apply(currentState, stateCode, operation)
        stateRepository[nextStateCode] = nextState

        return node.copy(constructionStateCode = nextStateCode, constructionState = nextState)
    }

    /**
     * States, if any, which are viable for an induce operation given the current state and a (potentially non-viable)
     * edit
     */
    fun inducibleEdits(
        fromNode: AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>,
        proposedTargetOperation: O,
    ): List<Pair<MetricAlignment<O, O2>, AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>>> {
        val stateCode = fromNode.constructionStateCode
        val currentState = stateRepository[stateCode] ?: throw IllegalStateException()
        val alignments = inducer.viableOperations(
            currentState,
            Optional.of(proposedTargetOperation),
            fromNode.targetState.isNotEmpty(),
            fromNode.targetIsComplete,
            Optional.empty(),
            fromNode.sourceState.isNotEmpty(),
            fromNode.sourceIsComplete,
        )
        return alignments.map { alignment ->
            alignment to applyStateTransition(fromNode, alignment.targetElement)
        }
    }

    /**
     * States, if any, which are viable for a deduce operation given the current state
     */
    fun deducibleEdits(
        fromNode: AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>,
        proposedSourceOperation: O,
    ): List<Pair<MetricAlignment<O, O2>, AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>>> {
        val stateCode = fromNode.constructionStateCode
        val currentState = stateRepository[stateCode] ?: throw IllegalStateException()
        val alignments = inducer.viableOperations(
            currentState,
            Optional.empty(),
            fromNode.targetState.isNotEmpty(),
            fromNode.targetIsComplete,
            Optional.of(proposedSourceOperation),
            fromNode.sourceState.isNotEmpty(),
            fromNode.sourceIsComplete,
        )
        return alignments.map { alignment ->
            alignment to applyStateTransition(fromNode, alignment.targetElement)
        }
    }

    /**
     * Operations, if any, which are viable as a merge of the two operations
     */
    fun viableMerges(
        fromNode: AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>,
        targetOperation: O,
        sourceOperation: O,
    ): List<Pair<MetricAlignment<O, O2>, AdjacencyMatrixDAGAlignmentTraversalState<Q, O, O2>>> {
        val stateCode = fromNode.constructionStateCode
        val currentState = stateRepository[stateCode] ?: throw IllegalStateException()
        val alignments = inducer.viableOperations(
            currentState,
            Optional.of(targetOperation),
            fromNode.targetState.isNotEmpty(),
            fromNode.targetIsComplete,
            Optional.of(sourceOperation),
            fromNode.sourceState.isNotEmpty(),
            fromNode.sourceIsComplete,
        )
        return alignments.map { alignment ->
            alignment to applyStateTransition(fromNode, alignment.targetElement)
        }
    }
}