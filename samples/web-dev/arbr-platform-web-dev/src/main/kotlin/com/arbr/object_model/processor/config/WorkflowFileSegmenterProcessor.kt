package com.arbr.object_model.processor.config

import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.internal.tree_parse.TreeParseFunctions
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.core.RequiredResourceMissingException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

/**
 * Listens to file updates and adds segment shells.
 */
@Component
@ComponentScan("com.arbr.core_web_dev")
@ConditionalOnProperty(prefix = "arbr.processor.file-segmenter", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileSegmenterProcessor(
    objectModelParser: ObjectModelParser,
    private val treeParseFunctions: TreeParseFunctions,
) : ArbrResourceFunction<
        ArbrFile, PartialFile,
        ArbrFile, PartialFile,
        ArbrFile, PartialFile,
        >(objectModelParser) {
    override val name: String
        get() = "file-segmenter"

    override val targetResourceClass: Class<ArbrFile>
        get() = cls()

    override val writeTargetResourceClass: Class<ArbrFile>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrFile,
        readResource: ArbrFile,
        writeResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrFile, PartialFile, ArbrForeignKey>) -> Mono<Void> {
        val fileSegments = listenResource.fileSegments.items.getLatestValue()
        if (!fileSegments.isNullOrEmpty()) {
            // Already segmented
            throw OperationCompleteException()
        }

        val filePath = require(listenResource.filePath)
        val fileContent = require(listenResource.content)

        return { partialObjectGraph ->
            val writeFile = partialObjectGraph.root
            logger.debug("file-segmenter:${listenResource.uuid} Segmenting ${writeFile.filePath?.value}")

            val segments = treeParseFunctions.computeSegments(
                partialObjectGraph,
                writeFile.uuid,
                filePath,
                fileContent,
            )

            logger.debug("file-segmenter:${listenResource.uuid} For ${segments.size} segments for ${writeFile.filePath?.value}")

            writeFile.fileSegments = immutableLinkedMapOfPartials(segments)

            logger.debug("file-segmenter:${listenResource.uuid} Will return empty for segments")
            Mono.empty<Void?>()
                .doOnSubscribe {
                    logger.debug("file-segmenter:${listenResource.uuid} Somebody subscribed to my empty result")
                }

        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrFile,
        readResource: ArbrFile,
        writeResource: ArbrFile,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        acquire(writeResource.fileSegments.items)
    }

    override fun checkPostConditions(
        listenResource: ArbrFile,
        readTargetResource: ArbrFile,
        writeTargetResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val segments = requireLatestChildren(listenResource.fileSegments.items)
        if (segments.isEmpty()) {
            throw RequiredResourceMissingException(
                listenResource.fileSegments.items,
                listenResource.fileSegments.items::class.java,
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileSegmenterProcessor::class.java)
    }
}