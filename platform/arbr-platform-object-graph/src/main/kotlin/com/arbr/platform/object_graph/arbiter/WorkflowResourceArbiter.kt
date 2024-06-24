package com.arbr.platform.object_graph.arbiter

import com.arbr.og.object_model.common.model.Proposal
import com.arbr.og.object_model.common.model.ProposedValueWriteStream
import com.arbr.platform.object_graph.core.WorkflowResourceModel
import com.arbr.platform.object_graph.core.WorkflowSingleResourceProcessor
import com.arbr.platform.object_graph.impl.ObjectModelResource
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.*

/**
 * An arbiter decides whether to accept or reject new values of properties on an object.
 */
abstract class WorkflowResourceArbiter<T : ObjectModelResource<T, *, *>> {

    private val logger =
        LoggerFactory.getLogger("com.arbr.og_engine.arbiter.WorkflowResourceArbiter")

    protected fun <V : Any> updateProposal(
        parentSingleResourceProcessor: WorkflowSingleResourceProcessor<*, *, *>,
        pvs: ProposedValueWriteStream<V>,
    ): Mono<Optional<Proposal<V>>> {
        return pvs.getCombinedBatchProposal()
            ?.map {
                val proposalWrapper = ProposalWrapper(parentSingleResourceProcessor, pvs.identifier, it, logger)
                Optional.of(proposalWrapper as Proposal<V>)
            }
            ?.single()?.onErrorResume {
                // TODO: Handle
                Mono.error(it)
            } ?: run {
            // No update
            Mono.just(Optional.empty())
        }
    }

    abstract fun processPendingProposals(
        parentSingleResourceProcessor: WorkflowSingleResourceProcessor<*, *, *>,
        workflowResourceModel: WorkflowResourceModel,
        resource: T
    ): Mono<Void>
}
