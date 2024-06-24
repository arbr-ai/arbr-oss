package com.arbr.object_model.processor.config

import com.arbr.util.adapt
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FilePaths
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Add details to project
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.project-file-based-details", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectFileBasedDetailsProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    private val githubExistingProjectDescriberApplication = promptLibrary.githubExistingProjectDescriber

    override val name: String
        get() = "project-file-based-details"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private val projectUuidToFileIdsMap = ConcurrentHashMap<String, Set<String>>()

    override fun prepareUpdate(
        listenResource: ArbrProject,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.primaryLanguage.getLatestValue() != null && listenResource.platform.getLatestValue() != null && listenResource.description.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val projectFullName = require(listenResource.fullName)

        val filesList = require(listenResource.files)

        val currentFileUuids = projectUuidToFileIdsMap[listenResource.uuid]
        val newFileUuids = filesList.map { it.value.uuid }.toSet()

        return { partialObjectGraph ->
            val writeProject = partialObjectGraph.root

            if (currentFileUuids == newFileUuids) {
                Mono.empty()
            } else {
                projectUuidToFileIdsMap[listenResource.uuid] = newFileUuids

                val filePathsValue = FilePaths.initializeMerged(
                    filesList.mapNotNull {
                        val filePath = it.value.filePath.getLatestAcceptedValue()
                        if (filePath == null) {
                            null
                        } else {
                            SourcedStruct1(filePath)
                        }
                    }
                )

                githubExistingProjectDescriberApplication.invoke(
                    projectFullName,
                    filePathsValue,
                    artifactSink.adapt(),
                )
                    .map { (title, primaryLanguage, platform, description, techStackDescription) ->

                        writeProject.title = title
                        writeProject.primaryLanguage = primaryLanguage
                        writeProject.platform = platform
                        writeProject.description = description
                        writeProject.techStackDescription = techStackDescription
                    }
                    .then()
            }
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrProject,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        acquire(writeResource.title)
        acquire(writeResource.primaryLanguage)
        acquire(writeResource.platform)
        acquire(writeResource.description)
        acquire(writeResource.techStackDescription)
    }

    override fun checkPostConditions(
        listenResource: ArbrProject,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        requireLatest(listenResource.title)
        requireLatest(listenResource.primaryLanguage)
        requireLatest(listenResource.platform)
        requireLatest(listenResource.description)
        requireLatest(listenResource.techStackDescription)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowProjectFileBasedDetailsProcessor::class.java)
    }
}
