package com.arbr.object_model.processor.config

import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.partial.PartialTaskRelevantFile
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.inference.embedding.FileEmbeddingSearchFunctions
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.model.collections.NestedObjectListType1
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FilePathsAndSummaries
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Map tasks to relevant files.
 * In: Project.{fullName,ProjectTask.taskQuery,<File.{filePath,summary}>}
 * Out: ProjectTask.<TaskRelevantFile>
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.task-relevant-files", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectTaskToRelevantFilesProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    private val fileEmbeddingSearchFunctions: FileEmbeddingSearchFunctions,
) : ArbrResourceFunction<
        ArbrTask, PartialTask,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    override val name: String
        get() = "task-relevant-files"
    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private val taskFileSearchApplication = promptLibrary.taskFileSearchApplication

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        // Note this is not technically correct - we may want to update the task's relevant files as more are added,
        // but until a better state update propagation system is implemented it's better to not keep this edge alive
        if (listenResource.taskRelevantFiles.items.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val projectFullName = require(readResource.fullName)
        val projectFiles = require(readResource.files)
        val taskQuery = require(listenResource.taskQuery)

        val filesWithSummariesValue = FilePathsAndSummaries.initializeMerged(
            projectFiles.map { (_, file) ->
                val filePathValue = require(file.filePath)
                val fileSummaryValue = require(file.summary)

                SourcedStruct2(
                    filePathValue,
                    fileSummaryValue,
                )
            }
        )

        // Match files by last ID for now - need to support version IDs to resolve multiples
        val filesByPath =
            projectFiles.values.groupBy { file ->
                val filePathValue = require(file.filePath)

                filePathValue.value
            }

        return { partialObjectGraph ->
            val writeProjectTask = partialObjectGraph.get<PartialTask>(listenResource.uuid)!!

            fileEmbeddingSearchFunctions
                .embeddingSearchFilePathsAndSummariesValue(volumeState, taskQuery, filesWithSummariesValue, maxNumFileSummariesIncluded)
                .flatMap { filteredFilesWithSummariesValue ->
                    taskFileSearchApplication.invoke(
                        projectFullName,
                        taskQuery,
                        filteredFilesWithSummariesValue,
                        artifactSink.adapt(),
                    ).map { (filePathsValue: NestedObjectListType1.Value<String, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue>) ->
                        filePathsValue.containers.mapNotNull { (filePath) ->
                            // Consider a fuzzier match
                            val file = filesByPath[filePath.value]?.lastOrNull()
                            if (file == null) {
                                logger.warn("Suggested task-relevant file ${filePath.value} not found in project state")
                                null
                            } else {
                                PartialTaskRelevantFile(
                                    partialObjectGraph,
                                    UUID.randomUUID().toString(),
                                ).apply {
                                    parent = PartialRef(listenResource.uuid)
                                    this.file = PartialRef(file.uuid)
                                }
                            }
                        }.take(maxNumFiles)
                    }
                        .map {
                            writeProjectTask.taskRelevantFiles = immutableLinkedMapOfPartials(it)
                        }.then()
                }
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        acquire(listenResource.taskRelevantFiles.items)

        readResource.files.items.getLatestAcceptedValue()?.forEach { (_, ref) ->
            ref.resource()?.let { file ->
                acquire(file.taskRelevantFiles.items)
            }
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrTask,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val relevantFiles = requireLatestChildren(listenResource.taskRelevantFiles.items)

        relevantFiles.forEach { (_, fileRelation) ->
            requireLatestAttached(fileRelation.file)
            requireLatestAttached(fileRelation.parent)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowProjectTaskToRelevantFilesProcessor::class.java)

        /**
         * Max number of input files to include in search
         */
        private const val maxNumFileSummariesIncluded = 24

        private const val maxNumFiles = 10
    }
}