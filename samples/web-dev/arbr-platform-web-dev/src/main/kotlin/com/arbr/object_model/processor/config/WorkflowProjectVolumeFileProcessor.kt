package com.arbr.object_model.processor.config

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.engine.util.FileContentUtils
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.field.ArbrFileContentValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Listens to project updates and adds volume files.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.volume-files", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectVolumeFileProcessor(
    objectModelParser: ObjectModelParser,
) : ArbrResourceFunction<
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    override val name: String
        get() = "volume-files"

    override val targetResourceClass: Class<ArbrProject> = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private fun partialFile(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        projectView: ArbrProject,
        filePath: ArbrFileFilePathValue,
        content: ArbrFileContentValue? = null,
    ): PartialFile {
        return PartialFile(
            partialObjectGraph,
            UUID.randomUUID().toString(),
        ).apply {
            this.filePath = filePath
            isBinary = null
            sourceLanguage = null
            this.content = content
            summary = null
            this.parent = PartialRef(projectView.uuid)
            fileSegments = null
            targetFileOfFileOp = null
            implementedFileOfFileOp = null
            commitRelevantFiles = null
            subtaskRelevantFiles = null
            taskRelevantFiles = null
        }
    }

    override fun prepareUpdate(
        listenResource: ArbrProject,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        val projectName = require(listenResource.fullName).value

        val existingFilePaths = listenResource.files.items.getLatestValue()
            ?.mapNotNull { it.value.resource()?.filePath?.getLatestAcceptedValue()?.value }
            ?: emptyList()

        return { partialObjectGraph ->
            val writeProject = partialObjectGraph.root

            volumeState.listFiles()
                .mapNotNull<PartialFile> { volumeFile ->
                    // Add a file from disk only if it's a new file by path
                    if (volumeFile.filePath in existingFilePaths) {
                        null
                    } else {
                        val normalizedFileContents = FileContentUtils.normalizeFileContent(volumeFile.contents)
                        partialFile(
                            partialObjectGraph,
                            listenResource,
                            ArbrFile.FilePath.materialized(volumeFile.filePath),
                            ArbrFile.Content.materialized(normalizedFileContents),
                        )
                    }
                }
                .collectList()
                .map { newFiles ->
                    if (newFiles.isNotEmpty()) {
                        logger.info("Populating project files for $projectName...")
                        writeProject.apply {
                            this.files =
                                (files ?: ImmutableLinkedMap()).updatingFrom(immutableLinkedMapOfPartials(newFiles))
                        }
                    } else {
                        listenResource
                    }
                }
                .then()
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrProject,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        acquire: (ProposedValueReadStream<*>) -> Unit,
    ) {
        acquire(writeResource.files.items)
    }

    override fun checkPostConditions(
        listenResource: ArbrProject,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        // Just require that the files collection exists - it could legitimately be empty
        val files = requireLatestChildren(listenResource.files.items).values

        files.forEach { file ->
            // For files that do exist, check that they have a path
            // Note files in memory but not on the volume may not have content if they are planned to be created
            requireLatest(file.filePath)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowProjectVolumeFileProcessor::class.java)
    }
}