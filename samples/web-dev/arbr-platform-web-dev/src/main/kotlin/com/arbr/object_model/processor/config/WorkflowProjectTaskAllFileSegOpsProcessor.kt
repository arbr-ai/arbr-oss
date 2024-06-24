package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.util.FileOpPartialHelper
import com.arbr.core_web_dev.util.file_segments.FileSegmentOperationDependencyUtils
import com.arbr.core_web_dev.workflow.model.FileOperation
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileOp
import com.arbr.object_model.core.partial.PartialFileSegmentOp
import com.arbr.object_model.core.partial.PartialFileSegmentOpDependency
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialSubtask
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrSubtask
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FileSegmentOperationsInFile
import com.arbr.prompt_library.util.FileSegmentOperationsInFileContainer
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.invariants.Invariants
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

@Component
@ConditionalOnProperty(prefix = "arbr.processor.task-all-file-seg-ops", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectTaskAllFileSegOpsProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrTask, PartialTask,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    override val name: String
        get() = "task-all-file-seg-ops"
    override val targetResourceClass: Class<ArbrProject> = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private val dependencyGraphApplication = promptLibrary.dependencyGraphApplication
    private val dependencyReassignApplication = promptLibrary.dependencyReassignApplication

    private fun orderedTopSort(
        fileSegmentOperationsInFileContainers: List<FileSegmentOperationsInFileContainer>,
        dependencyMap: Map<String, List<String>>,
    ): Pair<List<FileSegmentOperationsInFileContainer>, Map<String, List<String>>> {
        val (sortedIds, updatedDependencyMap) = FileSegmentOperationDependencyUtils.processDependencyMapping(
            fileSegmentOperationsInFileContainers.map { it.t1.value },
            dependencyMap,
        )

        val opMap = fileSegmentOperationsInFileContainers.associateBy { it.t1.value }
        return sortedIds.map { opMap[it]!! } to updatedDependencyMap
    }

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        val taskQuery = require(listenResource.taskQuery)

        val subtasks = require(listenResource.subtasks)

        val commits = subtasks.flatMap { (_, subtask) ->
            requireThatValue(subtask.commits) { commits ->
                commits.isNotEmpty()
            }.values
        }

        val fileOps = commits.flatMap { commit ->
            requireThatValue(commit.fileOps) { fileOps ->
                fileOps.isNotEmpty()
            }.values
        }

        // Mild hack to prevent this processor from being executed after the initial planning phase - if there's an
        // unimplemented file op with a commit completion status source, assign empty dependencies to everyone and
        // return early
        // TODO: Rework this processor to target file ops without tags and leave tagged file ops unaffected
        if (fileOps.any {
                it.implementedFile.getLatestAcceptedValue() == null
                        && it.commitEval.getLatestAcceptedValue()?.resource()?.uuid != null
            }) {
            Invariants.check { require ->
                // At the time of writing (2023-12-04), if one unimplemented file op has a tag then they all should,
                // but this might change
                require(
                    fileOps.all {
                        it.implementedFile.getLatestAcceptedValue() == null
                                && it.commitEval.getLatestAcceptedValue()
                            ?.resource()?.uuid != null
                    }
                )
            }

            return { partialObjectGraph ->
                fileOps
                    .filter { it.implementedFile.getLatestAcceptedValue() == null }
                    .mapNotNull { partialObjectGraph.get<PartialFileOp>(it.uuid) }
                    .forEach { fileOpPartial ->
                        fileOpPartial.fileSegmentOps?.values?.forEach { fileSegmentOpPartial ->
                            if (fileSegmentOpPartial.parentOfFileSegmentOpDependency == null) {
                                fileSegmentOpPartial.parentOfFileSegmentOpDependency = ImmutableLinkedMap()
                            }
                        }
                    }

                Mono.empty()
            }
        }

        val initialFilesByUUID =
            readResource.files.items.getLatestValue()?.values?.associateBy { it.uuid } ?: emptyMap()

        val fileSegmentOpsInFileContainers = fileOps.flatMapIndexed { index, fileOp ->
            val targetFile = requireAttached(fileOp.targetFile)
            val filePath = require(targetFile.filePath)

            val fileSegmentOpList = requireThatValue(fileOp.fileSegmentOps) { fileSegmentOps ->
                fileSegmentOps.isNotEmpty()
            }.values

            fileSegmentOpList.mapIndexed { segmentOpIndex, segmentOp ->
                FileSegmentOperationsInFileContainer(
//                    ArbrFileSegmentOp.ResourceId.materialized("$index-$segmentOpIndex"), // fake
                    filePath,
                    require(segmentOp.operation),
                    require(segmentOp.contentType),
                    require(segmentOp.ruleName),
                    require(segmentOp.name),
                    require(segmentOp.elementIndex),
                    require(segmentOp.description),
                )
            }
        }

        val fileSegmentOpsInFileValue = FileSegmentOperationsInFile.initializeMerged(
            fileSegmentOpsInFileContainers
        )

        val realFileSegOpMap = fileOps.flatMapIndexed { index, fileOp ->
            val fileSegmentOpList = requireThatValue(fileOp.fileSegmentOps) { fileSegmentOps ->
                fileSegmentOps.isNotEmpty()
            }.values

            fileSegmentOpList.mapIndexed { segmentOpIndex, segmentOp ->
                "$index-$segmentOpIndex" to segmentOp
            }
        }.toMap()

        return { partialObjectGraph ->
            val writeProject = partialObjectGraph.root

            dependencyGraphApplication.invoke(
                fileSegmentOpsInFileValue,
                artifactSink.adapt(),
            )
                .doOnNext { (dependencyEdges) ->
                    dependencyEdges.containers.map { (fileSegmentOpId,
                                                         dependencies) ->
                        dependencies.containers.map { (dependencyFileSegmentOpId) ->
                            logger.info("${fileSegmentOpId.value} ${dependencyFileSegmentOpId.value}")
                        }
                    }
                }
                .flatMap { (dependencyEdges) ->
                    val (orderedSegmentOps, dependencyMap) = dependencyEdges.containers.associate { (fileSegmentOpId,
                                                                                                        dependencies) ->
                        fileSegmentOpId.value to dependencies.containers.map { (dependencyFileSegmentOpId) ->
                            dependencyFileSegmentOpId.value
                        }
                    }
                        .mapNotNull { (k, vl) ->
                            k?.let {
                                it to vl.mapNotNull { v -> v }
                            }
                        }
                        .toMap()
                        .let {
                            orderedTopSort(fileSegmentOpsInFileContainers, it)
                        }

                    val fileSegmentOpMap = orderedSegmentOps.associateBy { it.t1.value }

                    dependencyReassignApplication.invoke(
                        FileSegmentOperationsInFile.initializeMerged(orderedSegmentOps),
                        artifactSink.adapt()
                    )
                        .doOnNext { (cdfsegops) ->
                            cdfsegops.containers.forEach { (commitMessage, diffSummary, fileSegOps) ->
                                logger.info("${commitMessage.value} ${diffSummary.value}")
                                fileSegOps.containers.forEach {
                                    logger.info(it.t1.value)
                                }
                                logger.info("")
                            }
                        }
                        .map { (cdfsegops) ->
                            val assignedFileSegmentOps = mutableListOf<Pair<String, PartialFileSegmentOp>>()

                            // Assumes exactly one project task
                            val task = writeProject.tasks!!.values.first()

                            val newSubtask = PartialSubtask(
                                partialObjectGraph,
                                UUID.randomUUID().toString(),
                            ).apply {
                                this.parent = PartialRef(task.uuid)
                                subtask = ArbrSubtask.Subtask.initialize(
                                    taskQuery.kind,
                                    taskQuery.value,
                                    taskQuery.generatorInfo,
                                ) // TODO: Infer subtasks
                            }

                            val filePartialsByUUID = initialFilesByUUID
                                .mapNotNull { (key, value) ->
                                    value.uuid?.let { uuid ->
                                        partialObjectGraph.get<PartialFile>(uuid)?.let {
                                            key to it
                                        }
                                    }
                                }
                                .toMap()
                                .toMutableMap()

                            val newCommits =
                                cdfsegops.containers.map { (commitMessage, diffSummary, fileSegOpFakeResourceIds) ->
                                    val newFileSegOpContainers = fileSegOpFakeResourceIds.containers.mapNotNull {
                                        fileSegmentOpMap[it.t1.value]
                                    }

                                    val commit = PartialCommit(
                                        partialObjectGraph,
                                        UUID.randomUUID().toString(),
                                    ).apply {
                                        this.parent = PartialRef(newSubtask.uuid)
                                        this.commitMessage = commitMessage
                                        this.diffSummary = diffSummary
                                    }

                                    val newFileOps =
                                        fileOps
                                            .groupBy { fileOp ->
                                                filePartialsByUUID[fileOp.targetFile.getLatestAcceptedValue()!!
                                                    .resource()!!.uuid]?.filePath?.value
                                            }
                                            .filterKeys { it != null }
                                            .mapKeys { it.key!! }
                                            .mapValues { (targetFilePath, oldFileOps) ->
                                                val targetFile =
                                                    oldFileOps.firstNotNullOfOrNull { fileOp ->
                                                        fileOp.targetFile.getLatestAcceptedValue()
                                                            ?.resource()?.uuid?.let { filePartialsByUUID[it] }
                                                    }
                                                        ?: throw Exception("No target file on origin file op for rewrite")

                                                val fileOpEnumValues = oldFileOps.mapNotNull {
                                                    it.fileOperation.getLatestAcceptedValue()?.value?.let { fop ->
                                                        FileOperation.parseLine(fop)
                                                    }
                                                }
                                                val newFileOpInnerValue =
                                                    if (fileOpEnumValues.any { it == FileOperation.CREATE_FILE }) {
                                                        "create_file"
                                                    } else if (fileOpEnumValues.any { it == FileOperation.DELETE_FILE }) {
                                                        "delete_file"
                                                    } else {
                                                        "edit_file"
                                                    }
                                                val someFileOperation =
                                                    oldFileOps.firstNotNullOf { it.fileOperation.getLatestAcceptedValue() }

                                                val newFileOp =
                                                    FileOpPartialHelper.fileOpPartial(
                                                        partialObjectGraph,
                                                        commit = commit,
                                                        file = targetFile,
                                                        fileOperation = ArbrFileOp.FileOperation.initialize(
                                                            someFileOperation.kind,
                                                            newFileOpInnerValue,
                                                            someFileOperation.generatorInfo,
                                                        ),
                                                        description = null, // Filled in later (check)
                                                        commitEval = PartialRef(null), // Fresh file op
                                                    )!!

                                                val newFileSegmentOps = newFileSegOpContainers
                                                    .filter { it.t2.value == targetFilePath }
                                                    .map { (filePath, operation, contentType, ruleName, name, elementIndex, description) ->
                                                        val fakeResourceId = filePath // TODO: Formalize

                                                        val fileSegmentOpView = realFileSegOpMap[fakeResourceId.value]!!
                                                        val fileSegmentOp =
                                                            partialObjectGraph.get<PartialFileSegmentOp>(
                                                                fileSegmentOpView.uuid
                                                            )!!

                                                        fakeResourceId.value to fileSegmentOp.apply {
                                                            parent = PartialRef(newFileOp.uuid)
                                                        }
                                                    }
                                                assignedFileSegmentOps.addAll(newFileSegmentOps)

                                                newFileOp
                                                    .apply {
                                                        fileSegmentOps = immutableLinkedMapOfPartials(
                                                            newFileSegmentOps.map { it.second }
                                                        )
                                                    }
                                            }

                                    // Ensure order is retained
                                    val orderedFileNames = fileOps.mapNotNull { fileOp ->
                                        fileOp.targetFile.getLatestAcceptedValue()?.resource()
                                            ?.let { filePartialsByUUID[it.uuid] }?.filePath?.value
                                    }.distinct()
                                    val orderedFileOps = orderedFileNames.mapNotNull { newFileOps[it] }

                                    commit.apply {
                                        this.fileOps = immutableLinkedMapOfPartials(orderedFileOps)
                                    }
                                }

                            // Re-add any file seg ops that were lost during commit reassignment. Order does not matter
                            // for setting dependencies.
                            val assignedFileSegmentOpIds = assignedFileSegmentOps.map { it.first }.toSet()
                            val unassignedFileSegmentOps =
                                realFileSegOpMap.filter { it.key !in assignedFileSegmentOpIds }
                                    .mapValues { (_, objectModelView) ->
                                        partialObjectGraph.get<PartialFileSegmentOp>(objectModelView.uuid)
                                    }
                                    .mapNotNull { (key, value) ->
                                        value?.let { key to it }
                                    }

                            val allFileSegmentOpsMap =
                                (assignedFileSegmentOps + unassignedFileSegmentOps).associate { it.first to it.second }
                            val fileSegOpsWithDependencies = allFileSegmentOpsMap.map { (fakeResourceId, fileSegOp) ->
                                val dependencyIds = dependencyMap.getOrDefault(fakeResourceId, emptyList())

                                val dependencyModels = dependencyIds.mapNotNull { dependencyFakeResourceId ->
                                    if (dependencyFakeResourceId == fakeResourceId) {
                                        // File cannot be dependent on itself
                                        null
                                    } else {
                                        allFileSegmentOpsMap[dependencyFakeResourceId]
                                    }
                                }.map {
                                    PartialFileSegmentOpDependency(
                                        partialObjectGraph,
                                        UUID.randomUUID().toString(),
                                    ).apply {
                                        parent = PartialRef(fileSegOp.uuid)
                                        dependencyFileSegmentOp = PartialRef(it.uuid)
                                    }
                                }

                                fileSegOp.apply {
                                    parentOfFileSegmentOpDependency =
                                        immutableLinkedMapOfPartials(dependencyModels)
                                }
                            }

                            // Check if file ops get attached to files
                            val newCommitsWithFileOps = newCommits.mapNotNull { commit ->
                                val newFileOpsList =
                                    commit.fileOps?.mapNotNull { (_, fileOp) ->
                                        val newFileSegOpsForFileOp =
                                            fileSegOpsWithDependencies.filter {
                                                it.parent?.uuid == fileOp.uuid
                                            }

                                        if (newFileSegOpsForFileOp.isEmpty()) {
                                            null
                                        } else {
                                            fileOp
                                                .apply {
                                                    fileSegmentOps =
                                                        immutableLinkedMapOfPartials(newFileSegOpsForFileOp)
                                                }
                                        }
                                    }

                                if (newFileOpsList.isNullOrEmpty()) {
                                    null
                                } else {
                                    commit
                                        .apply {
                                            this.fileOps = immutableLinkedMapOfPartials(newFileOpsList)
                                        }
                                }
                            }

                            val newSubtaskWithCommits = newSubtask.apply {
                                this.commits = immutableLinkedMapOfPartials(newCommitsWithFileOps)
                            }

                            task.subtasks = immutableLinkedMapOfPartials(listOf(newSubtaskWithCommits))
                            writeProject.tasks = immutableLinkedMapOfPartials(listOf(task))
                            writeProject.files = immutableLinkedMapOfPartials(filePartialsByUUID.values.toList())
                        }
                }.then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrTask,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val subtasks = requireLatestChildren(listenResource.subtasks.items).values
        val commits = subtasks.flatMap { subtask ->
            requireLatestChildren(subtask.commits.items).values
        }
        val fileOps = commits.flatMap { commit ->
            requireLatestChildren(commit.fileOps.items).values
        }
        val fileSegOps = fileOps.flatMap { fileOp ->
            requireLatestChildren(fileOp.fileSegmentOps.items).values
        }

        // Note this will fail, intentionally, if some file seg ops are dropped during commit reassign
        fileSegOps.forEach {
            if (it.parentOfFileSegmentOpDependency.items.getLatestValue() == null) {
                // Some still not getting set
                // Consider Partial mutable form
                requireLatestChildren(it.parentOfFileSegmentOpDependency.items)
            } else {
                requireLatestChildren(it.parentOfFileSegmentOpDependency.items)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowProjectTaskAllFileSegOpsProcessor::class.java)
    }
}