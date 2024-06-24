package com.arbr.object_model.processor.config

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.partial.PartialCommitEval
import com.arbr.object_model.core.partial.PartialFileSegmentOp
import com.arbr.object_model.core.partial.PartialFileSegmentOpDependency
import com.arbr.object_model.core.resource.ArbrCommitEval
import com.arbr.object_model.core.resource.ArbrFileSegmentOp
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.util_common.invariants.Invariants
import com.arbr.util_common.typing.cls
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Note the read target here makes use of the behavior that trying to find a parent `CommitEval`
 * and finding only a set-null parent finishes the operation.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.file-seg-op-simple-dependencies", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileSegOpSimpleDependenciesProcessor(
    objectModelParser: ObjectModelParser,
) : ArbrResourceFunction<
        ArbrFileSegmentOp, PartialFileSegmentOp,
        ArbrCommitEval, PartialCommitEval,
        ArbrCommitEval, PartialCommitEval,
        >(objectModelParser) {
    override val name: String = "file-seg-op-simple-dependencies"
    override val targetResourceClass: Class<ArbrCommitEval> = cls()
    override val writeTargetResourceClass: Class<ArbrCommitEval> = cls()

    override fun selectReadResource(listenResource: ArbrFileSegmentOp): ArbrCommitEval {
        val fileOp = requireAttached(listenResource.parent)

        return requireAttachedOrElseComplete(fileOp.commitEval)
    }

    override fun prepareUpdate(
        listenResource: ArbrFileSegmentOp,
        readResource: ArbrCommitEval,
        writeResource: ArbrCommitEval,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ): (PartialObjectGraph<ArbrCommitEval, PartialCommitEval, ArbrForeignKey>) -> Mono<Void> {
        val fileOp = requireAttached(listenResource.parent)

        val priorFileSegmentOpUuids = require(fileOp.fileSegmentOps).entries.takeWhile {
            it.value.uuid != listenResource.uuid
        }
            .filter {
                it.value.implementedFileSegment.getLatestAcceptedValue() == null
            }
            .map {
                it.value.uuid
            }

        return { partialObjectGraph ->
            val writeFileSegmentOp = partialObjectGraph.get<PartialFileSegmentOp>(listenResource.uuid)!!

            val fileSegmentOpDependencies = priorFileSegmentOpUuids
                .mapNotNull {
                    val dependencyFileSegmentOp = partialObjectGraph.get<PartialFileSegmentOp>(it)
                    Invariants.check { require ->
                        require(dependencyFileSegmentOp != null)
                    }

                    if (dependencyFileSegmentOp?.implementedFileSegment == null) {
                        dependencyFileSegmentOp
                    } else {
                        null
                    }
                }

            // Create a dependency edge from this file segment op to all previous file segment ops in the file op.
            if (fileSegmentOpDependencies.isEmpty()) {
                 writeFileSegmentOp.parentOfFileSegmentOpDependency = ImmutableLinkedMap()
            } else {
                fileSegmentOpDependencies.forEach {
                    PartialFileSegmentOpDependency(
                        partialObjectGraph,
                        UUID.randomUUID().toString(),
                    ).apply {
                        parent = PartialRef(writeFileSegmentOp.uuid)
                        dependencyFileSegmentOp = PartialRef(it.uuid)
                    }
                }
            }

            Mono.empty()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileSegmentOp,
        readTargetResource: ArbrCommitEval,
        writeTargetResource: ArbrCommitEval,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ) {
        requireLatestChildren(listenResource.parentOfFileSegmentOpDependency.items)
    }
}