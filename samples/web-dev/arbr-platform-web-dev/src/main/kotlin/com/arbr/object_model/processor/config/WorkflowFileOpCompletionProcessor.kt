package com.arbr.object_model.processor.config

import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileOp
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.file_system.VolumeState
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty(prefix = "arbr.processor.file-op-completion", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileOpCompletionProcessor(
    objectModelParser: ObjectModelParser,
): ArbrResourceFunction<
        ArbrFileOp, PartialFileOp,
        ArbrFile, PartialFile,
        ArbrFileOp, PartialFileOp,
        >(objectModelParser) {
    override val name: String
        get() = "file-op-completion"
    override val targetResourceClass: Class<ArbrFile>
        get() = ArbrFile::class.java
    override val writeTargetResourceClass: Class<ArbrFileOp>
        get() = ArbrFileOp::class.java

    override fun prepareUpdate(
        listenResource: ArbrFileOp,
        readResource: ArbrFile,
        writeResource: ArbrFileOp,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ): (PartialObjectGraph<ArbrFileOp, PartialFileOp, ArbrForeignKey>) -> Mono<Void> {
        // Until the engine supports selecting a parent resource by FKey, require the target file here instead of
        // using the read resource
        val targetFile = requireAttached(listenResource.targetFile)
        val fileSegmentOps = require(listenResource.fileSegmentOps)

        val implementedFileSegments = fileSegmentOps.map {
            it to require(it.value.implementedFileSegment)
        }

        return { partialObjectGraph ->
            val fileOp = partialObjectGraph.root

            logger.info(
                "File op ${fileOp.fileOperation?.value} ${targetFile.filePath.getLatestAcceptedValue()?.value} completed ${implementedFileSegments.size} segment ops:\n"
                + implementedFileSegments.joinToString("\n") { (fsope, fs) ->
                    "  - ${fsope.value.operation.getLatestAcceptedValue()?.value} ${fs.resource()?.ruleName?.getLatestAcceptedValue()?.value} ${fs.resource()?.name?.getLatestAcceptedValue()?.value}"
                }
            )

            fileOp.implementedFile = PartialRef(targetFile.uuid)

            Mono.empty()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileOp,
        readTargetResource: ArbrFile,
        writeTargetResource: ArbrFileOp,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ) {
        requireLatest(listenResource.implementedFile)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileOpCompletionProcessor::class.java)
    }
}