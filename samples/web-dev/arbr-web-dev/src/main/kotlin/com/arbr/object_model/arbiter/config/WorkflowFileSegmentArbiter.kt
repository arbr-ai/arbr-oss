package com.arbr.object_model.arbiter.config

//import com.arbr.object_model.core.resource.field.*
//import com.arbr.object_model.core.partial.PartialFile
//import com.arbr.object_model.core.partial.PartialFileSegment
//import com.arbr.object_model.core.partial.PartialFileSegmentOp
//import com.arbr.object_model.core.partial.PartialFileSegmentOpDependency
//import com.arbr.object_model.core.resource.ArbrFile
//import com.arbr.object_model.core.resource.ArbrFileSegment
//import com.arbr.object_model.core.resource.ArbrFileSegmentOp
//import com.arbr.object_model.core.resource.ArbrFileSegmentOpDependency
//import com.arbr.object_model.core.types.ArbrForeignKey
//import com.arbr.object_model.engine.arbiter.base.AbstractWorkflowFileSegmentArbiter
//import com.arbr.og.object_model.common.model.Proposal
//import com.arbr.platform.object_graph.impl.ObjectRef
//import com.arbr.og_engine.core.WorkflowResourceModel
//import com.arbr.util_common.reactor.nonBlocking
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//
//@Component
//class WorkflowFileSegmentArbiter : AbstractWorkflowFileSegmentArbiter() {
//    private fun checkChildSegments(
//        acceptedSegments: Map<String, ObjectRef<out ArbrFileSegment, PartialFileSegment, ArbrForeignKey>>,
//        proposedSegments: Map<String, ObjectRef<out ArbrFileSegment, PartialFileSegment, ArbrForeignKey>>,
//    ) {
//        // .. Check here ...
//    }
//
//    override fun arbitrate(
//        workflowResourceModel: WorkflowResourceModel,
//        resource: ArbrFileSegment,
//        containsTodoUpdate: Proposal<ArbrFileSegmentContainsTodoValue>?,
//        contentTypeUpdate: Proposal<ArbrFileSegmentContentTypeValue>?,
//        elementIndexUpdate: Proposal<ArbrFileSegmentElementIndexValue>?,
//        endIndexUpdate: Proposal<ArbrFileSegmentEndIndexValue>?,
//        nameUpdate: Proposal<ArbrFileSegmentNameValue>?,
//        ruleNameUpdate: Proposal<ArbrFileSegmentRuleNameValue>?,
//        startIndexUpdate: Proposal<ArbrFileSegmentStartIndexValue>?,
//        summaryUpdate: Proposal<ArbrFileSegmentSummaryValue>?,
//        parentUpdate: Proposal<ObjectRef<out ArbrFile, PartialFile, ArbrForeignKey>>?,
//        parentSegmentUpdate: Proposal<ObjectRef<out ArbrFileSegment, PartialFileSegment, ArbrForeignKey>>?,
//        fileSegmentsItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrFileSegment, PartialFileSegment, ArbrForeignKey>>>?,
//        fileSegmentOpsItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrFileSegmentOp, PartialFileSegmentOp, ArbrForeignKey>>>?,
//        fileSegmentOpDependencysItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrFileSegmentOpDependency, PartialFileSegmentOpDependency, ArbrForeignKey>>>?
//    ): Mono<Void> {
//        return nonBlocking {
//            val acceptedFileSegments: Map<String, ObjectRef<out ArbrFileSegment, PartialFileSegment, ArbrForeignKey>>? =
//                fileSegmentsItemsUpdate?.acceptedValue
//            val proposedFileSegments: Map<String, ObjectRef<out ArbrFileSegment, PartialFileSegment, ArbrForeignKey>>? =
//                fileSegmentsItemsUpdate?.proposedValue
//
//            if (acceptedFileSegments != null && proposedFileSegments != null && acceptedFileSegments != proposedFileSegments) {
//                checkChildSegments(acceptedFileSegments, proposedFileSegments)
//            }
//
//            val proposals = listOfNotNull(
//                parentUpdate,
//                parentSegmentUpdate,
//                contentTypeUpdate,
//                ruleNameUpdate,
//                nameUpdate,
//                elementIndexUpdate,
//                startIndexUpdate,
//                endIndexUpdate,
//                summaryUpdate,
//                containsTodoUpdate,
//
//                fileSegmentsItemsUpdate,
//                fileSegmentOpsItemsUpdate,
//            )
//
//            proposals.forEach { proposal ->
//                proposal.accept()
//            }
//        }.then()
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(WorkflowFileSegmentArbiter::class.java)
//    }
//}
