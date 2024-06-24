package com.arbr.object_model.processor.config

import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty(prefix = "arbr.processor.fseg-contains-todo", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileSegmentContainsTodoProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrFileSegment, PartialFileSegment,
        ArbrFileSegment, PartialFileSegment,
        ArbrFileSegment, PartialFileSegment,
        >(objectModelParser) {
    private val todoApplication = promptLibrary.todoApplication

    override val name: String
        get() = "fseg-contains-todo"

    override val targetResourceClass: Class<ArbrFileSegment> = cls()

    override val writeTargetResourceClass: Class<ArbrFileSegment>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrFileSegment,
        readResource: ArbrFileSegment,
        writeResource: ArbrFileSegment,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrFileSegment, PartialFileSegment, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.containsTodo.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        return { partialObjectGraph ->
            val writeFileSegment = partialObjectGraph.root
//            resource.containsTodo.propose(
//                ArbrFileSegment.ContainsTodo.materialized(
//                    contentValue != null && contentValue.lowercase().contains("todo:")
//                )
//            )

            // Temporary shortcut
            writeFileSegment.containsTodo = ArbrFileSegment.ContainsTodo.materialized(
                false
            )

            Mono.empty()
        }

//        if (resource.containsTodo.getLatestValue() != null) {
//            throw OperationCompleteException()
//        }
//
//        val content = require(resource.content)
//
//        return {
//            todoApplication
//                .invoke(content, artifactSink.adapt())
//                .flatMap { (containsTodo) ->
//                    nonBlocking {
//                        resource.containsTodo.propose(containsTodo)
//
//                        if (containsTodo.value == true) {
//                            logger.info("Identified todo in segment: {}", content.value)
//                        }
//                    }.then()
//                }
//        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrFileSegment,
        readResource: ArbrFileSegment,
        writeResource: ArbrFileSegment,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        acquire(writeResource.containsTodo)
    }

    override fun checkPostConditions(
        listenResource: ArbrFileSegment,
        readTargetResource: ArbrFileSegment,
        writeTargetResource: ArbrFileSegment,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        requireLatest(listenResource.containsTodo)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileSegmentContainsTodoProcessor::class.java)
    }
}
