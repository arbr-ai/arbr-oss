package com.arbr.engine.services.workflow.state

import com.arbr.api.workflow.core.WorkflowProcessorStatus
import com.arbr.api.workflow.resource.WorkflowResourceType
import com.arbr.api.workflow.view_model.*
import com.arbr.api.workflow.view_model.update.ViewModelValueUpdate
import com.arbr.api.workflow.view_model.update.ViewModelValueUpdateOperation
import com.arbr.api.workflow.view_model.update.ViewModelValueUpdateOperation.*
import com.arbr.engine.util.FluxIngestor
import com.arbr.og_engine.artifact.Artifact
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp

/**
 * TODO: Generalize state machine
 */
class WorkflowViewModelManager(
    val workflowId: Long,
    private val workflowResourceStatusPartialViewModelMappers: List<WorkflowResourceStatusPartialViewModelMapper>,
    private val workflowProcessorStatusPartialViewModelMappers: List<WorkflowProcessorStatusPartialViewModelMapper>,
) {
    private var workflowStartTimeMs: Long? = null
    private var progress: Double? = null
    private var status: WorkflowViewModelStatus? = null
    private var activeTasks: MutableList<WorkflowViewModelActiveTask>? = null
    private var fileData: MutableList<WorkflowViewModelFileData>? = null
    private var stats: MutableList<WorkflowViewModelStatistic>? = null
    private var pullRequest: WorkflowViewModelPullRequestData? = null
    private var commits: MutableList<WorkflowViewModelCommitData>? = null

    private val partialViewModelProducingIngestor = FluxIngestor<WorkflowPartialViewModel>()

    @Synchronized
    fun updateWorkflowStartTimeMs(
        update: ViewModelValueUpdate<Long>,
    ): Long? {
        workflowStartTimeMs = when (update.operation) {
            ADD, REMOVE -> throw UnsupportedOperationException("Operation ${update.operation} is not supported for workflowStartTimeMs")
            UPDATE, SET -> update.value
        }
        return workflowStartTimeMs
    }

    @Synchronized
    fun updateProgress(
        update: ViewModelValueUpdate<Double>,
    ): Double? {
        progress = when (update.operation) {
            ADD -> (progress ?: 0.0) + (update.value ?: 0.0)
            REMOVE -> (progress ?: 0.0) - (update.value ?: 0.0)
            UPDATE, SET -> update.value
        }
        return progress
    }

    @Synchronized
    fun updateStatus(
        update: ViewModelValueUpdate<WorkflowViewModelStatus>,
    ): WorkflowViewModelStatus? {
        if (update.operation == SET || update.operation == UPDATE) {
            status = update.value
        } else {
            throw UnsupportedOperationException("Operation ${update.operation} is not supported for status")
        }
        return status
    }

    /**
     * Apply the update from the given argument and return the new value of activeTasks.
     */
    @Synchronized
    fun updateActiveTasks(
        activeTasksUpdate: ViewModelValueUpdate<List<WorkflowViewModelActiveTask>>,
    ): List<WorkflowViewModelActiveTask>? {
        val currentActiveTasks = activeTasks ?: mutableListOf()

        activeTasks = when (activeTasksUpdate.operation) {
            ADD -> {
                val tasksToAdd = activeTasksUpdate.value?.filter { newTask ->
                    currentActiveTasks.none { existingTask -> existingTask.kind == newTask.kind }
                }.orEmpty()
                currentActiveTasks.addAll(tasksToAdd)
                currentActiveTasks
            }

            REMOVE -> {
                val tasksToRemove = activeTasksUpdate.value.orEmpty().map { it.kind }.toSet()
                currentActiveTasks.removeAll { existingTask -> existingTask.kind in tasksToRemove }
                currentActiveTasks
            }

            UPDATE -> {
                activeTasksUpdate.value?.forEach { updateTask ->
                    val existingIndex = currentActiveTasks.indexOfFirst { it.kind == updateTask.kind }
                    if (existingIndex >= 0) {
                        currentActiveTasks[existingIndex] = updateTask
                    } else {
                        currentActiveTasks.add(updateTask)
                    }
                }
                currentActiveTasks
            }

            SET -> {
                activeTasksUpdate.value?.toMutableList()
            }
        }
        return activeTasks
    }

    @Synchronized
    fun updateFileData(
        update: ViewModelValueUpdate<List<WorkflowViewModelFileData>>,
    ): List<WorkflowViewModelFileData>? {
        val currentFileData = fileData ?: mutableListOf()

        fileData = when (update.operation) {
            ADD -> currentFileData.addAll(update.value ?: emptyList())
                .let { currentFileData }

            REMOVE -> currentFileData.removeAll(update.value ?: emptyList())
                .let { currentFileData }

            UPDATE -> {
                update.value?.forEach { fileDataItem ->
                    if (currentFileData.none {
                            it.filePath == fileDataItem.filePath
                                    && it.fileContents == fileDataItem.fileContents
                        }) {
                        currentFileData.add(fileDataItem)
                    }
                }
                    .let { currentFileData }
            }

            SET -> update.value?.toMutableList()
        }
        return fileData
    }

    @Synchronized
    fun updateStats(
        update: ViewModelValueUpdate<List<WorkflowViewModelStatistic>>,
    ): List<WorkflowViewModelStatistic>? {
        val currentStats = stats ?: mutableListOf()

        stats = when (update.operation) {
            ADD -> {
                // Only add stats which kinds are not already present in the list
                val uniqueNewStats = update.value?.filter { newStat ->
                    currentStats.none { existingStat -> existingStat.kind == newStat.kind }
                } ?: emptyList()

                currentStats.addAll(uniqueNewStats)
                currentStats
            }

            REMOVE -> {
                // Only keep stats which kinds are NOT in the list provided for removal
                update.value?.forEach { removeStat ->
                    currentStats.removeAll { existingStat -> existingStat.kind == removeStat.kind }
                }
                currentStats
            }

            UPDATE -> {
                // Update stats by kind, or add new if kind doesn't exist
                update.value?.forEach { updateStat ->
                    val index = currentStats.indexOfFirst { it.kind == updateStat.kind }
                    if (index != -1) {
                        currentStats[index] = updateStat
                    } else {
                        currentStats.add(updateStat)
                    }
                }
                currentStats
            }

            SET -> {
                update.value?.toMutableList()
            }
        }
        return stats
    }

    @Synchronized
    fun updatePullRequest(
        update: ViewModelValueUpdate<WorkflowViewModelPullRequestData>,
    ): WorkflowViewModelPullRequestData? {
        if (update.operation == SET || update.operation == UPDATE) {
            pullRequest = update.value
        } else {
            throw UnsupportedOperationException("Operation ${update.operation} is not supported for pullRequest")
        }
        return pullRequest
    }

    @Synchronized
    fun updateCommits(
        update: ViewModelValueUpdate<List<WorkflowViewModelCommitData>>,
    ): List<WorkflowViewModelCommitData>? {
        val currentCommits = commits ?: mutableListOf()

        commits = when (update.operation) {
            ADD -> currentCommits.addAll(update.value ?: emptyList())
                .let { currentCommits }

            REMOVE -> currentCommits.removeAll(update.value ?: emptyList())
                .let { currentCommits }

            UPDATE -> {
                update.value?.forEach { commitData ->
                    if (commitData !in currentCommits) {
                        currentCommits.add(commitData)
                    }
                }
                    .let { currentCommits }
            }

            SET -> update.value?.toMutableList()
        }
        return commits
    }

    private val fileContentCache = ConcurrentHashMap<String, String>()

    private fun <T : Any> updateSetOfNullable(
        nullableValue: T?
    ): ViewModelValueUpdate<T>? = nullableValue?.let { value ->
        ViewModelValueUpdate(
            ViewModelValueUpdateOperation.SET,
            value,
        )
    }

    private fun makeWorkflowPartialViewModel(
        workflowId: Long,
        workflowStartTimeMsUpdate: ViewModelValueUpdate<Long>? = null,
        progressUpdate: ViewModelValueUpdate<Double>? = null,
        statusUpdate: ViewModelValueUpdate<WorkflowViewModelStatus>? = null,
        activeTasksUpdate: ViewModelValueUpdate<List<WorkflowViewModelActiveTask>>? = null,
        fileDataUpdate: ViewModelValueUpdate<List<WorkflowViewModelFileData>>? = null,
        statsUpdate: ViewModelValueUpdate<List<WorkflowViewModelStatistic>>? = null,
        pullRequestUpdate: ViewModelValueUpdate<WorkflowViewModelPullRequestData>? = null,
        commitsUpdate: ViewModelValueUpdate<List<WorkflowViewModelCommitData>>? = null,
    ): WorkflowPartialViewModel {
        return WorkflowPartialViewModel(
            workflowId.toString(),
            workflowStartTimeMsUpdate,
            progressUpdate,
            statusUpdate,
            activeTasksUpdate,
            fileDataUpdate,
            statsUpdate,
            pullRequestUpdate,
            commitsUpdate,
        )
    }

    private fun progressCurve(
        elapsedMs: Long,
    ): Double {
        return 2 / (1 + exp(-elapsedMs / PROGRESS_MS_DIVISOR)) - 1
    }

    private fun ensureStatusAtLeast(
        workflowId: Long,
        kind: WorkflowViewModelStatusKind
    ): Mono<WorkflowPartialViewModel> {
        if (status?.kind == kind) {
            return Mono.empty()
        }

        val targetStatus: WorkflowViewModelStatus = getStatusByKind(kind)
        val targetOrder: Int = getStatusOrderByKind(kind)

        val oldStatus = status
        val currentOrder: Int = if (oldStatus == null) {
            -1
        } else {
            getStatusOrderByKind(oldStatus.kind)
        }

        return if (oldStatus == null || currentOrder <= targetOrder) {
            val newStatusUpdate = ViewModelValueUpdate(
                SET,
                targetStatus,
            )
            val newStatus = updateStatus(newStatusUpdate)

            val newProgressUpdate = if (newStatus != null && newStatus.kind != oldStatus?.kind) {
                val checkpoints = listOf(
                    0.0,
                    0.2,
                    0.4,
                    0.6,
                    0.8,
                    0.95,
                    1.0,
                )

                val newProgressCheckpointIndex = when (newStatus.kind) {
                    WorkflowViewModelStatusKind.BOOTING -> 0
                    WorkflowViewModelStatusKind.PLANNING -> 1
                    WorkflowViewModelStatusKind.DEVELOPING -> 2
                    WorkflowViewModelStatusKind.TESTING -> 3
                    WorkflowViewModelStatusKind.DEPLOYING -> 4
                    WorkflowViewModelStatusKind.FINISHING -> 5
                    WorkflowViewModelStatusKind.NOT_STARTED -> 0
                    WorkflowViewModelStatusKind.SUCCEEDED -> 6
                    WorkflowViewModelStatusKind.CANCELLED -> 6
                    WorkflowViewModelStatusKind.FAILED -> 6
                    WorkflowViewModelStatusKind.UNKNOWN -> 0
                }

                // Create a curve between current checkpoint and next
                val newProgressValueBase = checkpoints[newProgressCheckpointIndex]
                val newProgressValueNext = if (newProgressCheckpointIndex < checkpoints.size - 1) {
                    checkpoints[newProgressCheckpointIndex + 1]
                } else {
                    newProgressValueBase
                }

                val nowMs = Instant.now().toEpochMilli()
                val startMs = workflowStartTimeMs ?: nowMs
                val elapsedMs = nowMs - startMs
                val interstitialProgress = progressCurve(elapsedMs)
                val newProgressValue =
                    (newProgressValueNext - newProgressValueBase) * interstitialProgress + newProgressValueNext

                val progressUpdate = ViewModelValueUpdate(
                    SET,
                    newProgressValue,
                )
                updateProgress(progressUpdate)

                progressUpdate
            } else {
                null
            }

            Mono.just(
                makeWorkflowPartialViewModel(
                    workflowId,
                    statusUpdate = newStatusUpdate,
                    progressUpdate = newProgressUpdate,
                )
            )
        } else {
            Mono.empty()
        }
    }

    private val baseFileContentCache = ConcurrentHashMap<String, String>()

    private fun resourceArtifactPartialViewModel(
        workflowId: Long,
        resourceType: WorkflowResourceType,
        resourceData: Map<String, Any?>
    ): Mono<WorkflowPartialViewModel> {
        return workflowResourceStatusPartialViewModelMappers
            .firstOrNull { mapper ->
                mapper.resourceType == resourceType
            }
            ?.mapResourceData(workflowId, resourceType, resourceData)
            ?: Mono.empty()

        // TODO: Move to web dev case
//        return when (resourceType) {
//            WORKFLOW_RESOURCE_TYPE_FILE -> {
//                val filePath = (resourceData["filePath"] as? ArbrFile.FilePath.Value)?.value
//                val content = (resourceData["contentId"] as? ArbrFile.Content.Value)?.value
//
//                if (filePath == null || content == null) {
//                    Mono.empty()
//                } else {
//                    val fileName = Paths.get(filePath).fileName.toString()
//                    val priorContent = baseFileContentCache.putIfAbsent(filePath, content)
//
//                    if (content == priorContent) {
//                        Mono.empty()
//                    } else {
//                        val fileData = if (priorContent == null) {
//                            // No annotations for brand-new file
//                            Mono.just(
//                                WorkflowViewModelFileData(
//                                    fileName,
//                                    filePath,
//                                    content,
//                                    emptyList()
//                                )
//                            )
//                        } else {
//                            ViewModelDiffUtils.getViewModelFileData(
//                                fileName, filePath, priorContent, content
//                            )
//                        }
//
//                        fileData.map {
//                            val update = ViewModelValueUpdate(
//                                UPDATE,
//                                listOf(it)
//                            )
//                            updateFileData(update)
//                            makeWorkflowPartialViewModel(
//                                workflowId,
//                                fileDataUpdate = update,
//                            )
//                        }
//                    }
//                }
//            }
//
//            WORKFLOW_RESOURCE_TYPE_PROJECT -> {
//                ensureStatusAtLeast(workflowId, WorkflowViewModelStatusKind.PLANNING)
//            }
//
//            WORKFLOW_RESOURCE_TYPE_SUBTASK,
//            WORKFLOW_RESOURCE_TYPE_TASK,
//            WORKFLOW_RESOURCE_TYPE_COMMIT -> Mono.empty()
//
//            WORKFLOW_RESOURCE_TYPE_FILE_OP -> {
//                ensureStatusAtLeast(workflowId, WorkflowViewModelStatusKind.DEVELOPING)
//            }
//
//            WORKFLOW_RESOURCE_TYPE_TASK_EVAL,
//            WORKFLOW_RESOURCE_TYPE_SUBTASK_EVAL,
//            WORKFLOW_RESOURCE_TYPE_COMMIT_EVAL -> {
//                ensureStatusAtLeast(workflowId, WorkflowViewModelStatusKind.TESTING)
//            }
//
//            WORKFLOW_RESOURCE_TYPE_COMMIT_RELEVANT_FILE,
//            WORKFLOW_RESOURCE_TYPE_SUBTASK_RELEVANT_FILE,
//            WORKFLOW_RESOURCE_TYPE_TASK_RELEVANT_FILE,
//            WORKFLOW_RESOURCE_TYPE_FILE_SEGMENT,
//            WORKFLOW_RESOURCE_TYPE_FILE_SEGMENT_OP,
//            WORKFLOW_RESOURCE_TYPE_FILE_SEGMENT_OP_DEPENDENCY,
//            WORKFLOW_RESOURCE_TYPE_VECTOR_RESOURCE,
//            WORKFLOW_RESOURCE_TYPE_UNKNOWN -> Mono.empty()
//        }
    }

    private fun processorStatusArtifactPartialViewModel(
        workflowId: Long,
        processorStatus: WorkflowProcessorStatus,
        processorName: String,
        resourceUuid: String,
    ): WorkflowPartialViewModel? {
        val operation = when (processorStatus) {
            WorkflowProcessorStatus.STARTED -> ADD
            WorkflowProcessorStatus.SUCCEEDED,
            WorkflowProcessorStatus.FAILED -> REMOVE

            WorkflowProcessorStatus.READY,
            WorkflowProcessorStatus.CANCELLED -> null // Unused
        } ?: return null

        val mapper = workflowProcessorStatusPartialViewModelMappers.firstOrNull { mapper ->
            mapper.processorFunction.name == processorName
        } ?: return null

        val activeTask = mapper.mapProcessorStatusUpdate(
            workflowId,
            processorStatus,
            processorName,
            resourceUuid,
            status
        ) ?: return null

        logger.info("Processor Status artifact: ${processorStatus.serializedName} $processorName[$resourceUuid] ${operation.serializedName} visual update: ${activeTask.displayString}")

        val update = ViewModelValueUpdate(
            operation,
            listOf(activeTask),
        )
        updateActiveTasks(update)
        return makeWorkflowPartialViewModel(workflowId, activeTasksUpdate = update)

        // TODO: Extract to web dev
//        val processorKind = try {
//            ResourceProcessor.create(processorName)
//        } catch (e: Exception) {
//            logger.warn("Unknown processor for view model update: $processorName")
//            return null
//        }
//
//        logger.info("Processor Status artifact: ${processorStatus.serializedName} $processorName[$resourceUuid] ${operation.serializedName}")
//
//        val activeTask = when (processorKind) {
//            /**
//             * Indexing
//             */
//            ResourceProcessor.WorkflowProjectVolumeFileProcessor,
//            ResourceProcessor.WorkflowFileSegmenterProcessor,
//            ResourceProcessor.WorkflowFileSummaryProcessor -> {
//                val workflowStatus = status
//
//                if (workflowStatus == null || workflowStatus.kind.ordinal <= WorkflowViewModelStatusKind.PLANNING.ordinal) {
//                    WorkflowViewModelActiveTask.indexing()
//                } else {
//                    return null
//                }
//            }
//
//            /**
//             * General planning
//             */
//            ResourceProcessor.WorkflowProjectTaskVerbosePlanProcessor,
//            ResourceProcessor.WorkflowFileOpDetailProcessor,
//            ResourceProcessor.WorkflowFileOpToFileSegOpsProcessor,
//            ResourceProcessor.WorkflowFileSegOpSimpleDependenciesProcessor,
//            ResourceProcessor.WorkflowProjectTaskToSubtaskProcessor,
//            ResourceProcessor.WorkflowProjectSubtaskToCommitProcessor,
//            ResourceProcessor.WorkflowProjectTaskAllFileSegOpsProcessor -> return null
//
//            /**
//             * Repair planning
//             */
//            ResourceProcessor.WorkflowCommitRepairProcessor -> return null
//
//            /**
//             * Implementation
//             */
//            ResourceProcessor.WorkflowFileOpDiffImplementationProcessor,
//            ResourceProcessor.WorkflowFileOpDiffImplementationProcessorPackageJson,
//            ResourceProcessor.DebugWorkflowFileSegOpImplementationProcessor,
//            ResourceProcessor.WorkflowFileSegOpImplementationProcessor -> return null
//
//            /**
//             * Evaluation (kind of contrived):
//             * Task + Subtask -> Monitoring
//             * Commit + File Op -> Testing
//             */
//            ResourceProcessor.WorkflowTaskCompletionProcessor,
//            ResourceProcessor.WorkflowSubtaskCompletionProcessor -> WorkflowViewModelActiveTask.monitoringSystem()
//
//            ResourceProcessor.WorkflowCommitCompletionProcessor,
//            ResourceProcessor.WorkflowFileOpCompletionProcessor -> WorkflowViewModelActiveTask.testingInWebBrowser()
//
//            /**
//             * Commit building and publishing
//             */
//            ResourceProcessor.WorkflowCommitBuildingProcessor -> WorkflowViewModelActiveTask.building()
//
//            /**
//             * Publishing
//             */
//            ResourceProcessor.WorkflowCommitWritingProcessor,
//            ResourceProcessor.WorkflowPullRequestOpeningProcessor,
//            ResourceProcessor.WorkflowPullRequestOpeningEarlyCompletionProcessor,
//            ResourceProcessor.WorkflowTaskStatusPublisher -> WorkflowViewModelActiveTask.deployingToServer()
//
//            /**
//             * Analysis + Refinement
//             */
//            ResourceProcessor.WorkflowFileSegmentContainsTodoProcessor,
//            ResourceProcessor.WorkflowProjectFileBasedDetailsProcessor,
//            ResourceProcessor.WorkflowProjectTaskToRelevantFilesProcessor,
//            ResourceProcessor.WorkflowProjectSubtaskToRelevantFilesProcessor,
//            ResourceProcessor.WorkflowProjectSubtaskCommitToRelevantFilesProcessor,
//            ResourceProcessor.WorkflowTodoProcessor -> return null
//        }
//
    }

    fun processSingleArtifact(
        workflowId: Long,
        workflowArtifact: Artifact,
    ): Mono<WorkflowPartialViewModel> {
        // TODO: Generalize beyond code
//        return when (workflowArtifact) {
//            is WorkflowResourceCreationArtifact -> {
//                val resourceType = workflowArtifact.resourceType
//                val resourceData = workflowArtifact.resourceData
//
//                resourceArtifactPartialViewModel(workflowId, resourceType, resourceData)
//            }
//
//            is WorkflowResourceUpdateArtifact -> {
//                val resourceType = workflowArtifact.resourceType
//                val resourceData = workflowArtifact.resourceData
//
//                resourceArtifactPartialViewModel(workflowId, resourceType, resourceData)
//            }
//
//            is ProcessorStatusArtifact -> Mono.justOrEmpty(
//                processorStatusArtifactPartialViewModel(
//                    workflowId,
//                    workflowArtifact.status,
//                    workflowArtifact.processorName,
//                    workflowArtifact.resourceUuid,
//                )
//            )
//
//            is WorkflowStatusArtifact -> {
//                // Translate deliberate status update to at-least state change
//                // Could deduplicate status enums in the future
//                val newStatus = workflowArtifact.status
//
//                when (newStatus) {
//                    WorkflowStatus.NOT_STARTED -> ensureStatusAtLeast(
//                        workflowId,
//                        WorkflowViewModelStatusKind.NOT_STARTED
//                    )
//
//                    WorkflowStatus.STARTED -> ensureStatusAtLeast(workflowId, WorkflowViewModelStatusKind.BOOTING)
//                    WorkflowStatus.SUCCEEDED -> ensureStatusAtLeast(workflowId, WorkflowViewModelStatusKind.SUCCEEDED)
//                    WorkflowStatus.FAILED -> ensureStatusAtLeast(workflowId, WorkflowViewModelStatusKind.FAILED)
//                    WorkflowStatus.CANCELLED -> ensureStatusAtLeast(workflowId, WorkflowViewModelStatusKind.CANCELLED)
//                }
//            }
//
//            is WorkflowCommitArtifact -> {
//                val commitsUpdate = ViewModelValueUpdate<List<WorkflowViewModelCommitData>>(
//                    ADD,
//                    listOf(
//                        WorkflowViewModelCommitData(
//                            commitHash = workflowArtifact.commitHash,
//                            commitMessage = workflowArtifact.commitMessage,
//                            url = workflowArtifact.commitUrl,
//                            diffSummary = workflowArtifact.diffSummary,
//                            diffStat = workflowArtifact.diffStat,
//                        )
//                    )
//                )
//                updateCommits(
//                    commitsUpdate
//                )
//                Mono.just(
//                    makeWorkflowPartialViewModel(
//                        workflowId,
//                        commitsUpdate = commitsUpdate,
//                    )
//                )
//            }
//
//            is WorkflowPullRequestArtifact -> {
//                val prUpdate = ViewModelValueUpdate<WorkflowViewModelPullRequestData>(
//                    SET,
//                    WorkflowViewModelPullRequestData(
//                        title = workflowArtifact.title,
//                        link = workflowArtifact.link,
//                    )
//                )
//                updatePullRequest(
//                    prUpdate
//                )
//                Mono.just(
//                    makeWorkflowPartialViewModel(
//                        workflowId,
//                        pullRequestUpdate = prUpdate,
//                    )
//                )
//            }
//
//            is ApplicationArtifact,
//            is FileTreeArtifact,
//            is WorkflowPlanArtifact,
//            is WorkflowRevertArtifact -> Mono.empty() // Do nothing
//
//        }

        return Mono.empty()
    }

    fun ingestWorkflowArtifacts(
        workflowId: Long,
        workflowArtifacts: List<Artifact>,
    ): Flux<WorkflowPartialViewModel> {
        return Flux.fromIterable(workflowArtifacts)
            .concatMap { artifact ->
                partialViewModelProducingIngestor.ingest {
                    try {
                        processSingleArtifact(workflowId, artifact)
                    } catch (e: Exception) {
                        Mono.error(e)
                    }
                }
            }
    }

    companion object {
        private const val PROGRESS_MS_DIVISOR = 60_000.0

        private val logger = LoggerFactory.getLogger(WorkflowViewModelManager::class.java)

        private fun getStatusByKind(kind: WorkflowViewModelStatusKind): WorkflowViewModelStatus = when (kind) {
            WorkflowViewModelStatusKind.BOOTING -> WorkflowViewModelStatus.booting()
            WorkflowViewModelStatusKind.PLANNING -> WorkflowViewModelStatus.planning()
            WorkflowViewModelStatusKind.DEVELOPING -> WorkflowViewModelStatus.developing()
            WorkflowViewModelStatusKind.TESTING -> WorkflowViewModelStatus.testing()
            WorkflowViewModelStatusKind.DEPLOYING -> WorkflowViewModelStatus.deploying()
            WorkflowViewModelStatusKind.FINISHING -> WorkflowViewModelStatus.finishing()
            WorkflowViewModelStatusKind.NOT_STARTED -> WorkflowViewModelStatus.notStarted()
            WorkflowViewModelStatusKind.SUCCEEDED -> WorkflowViewModelStatus.succeeded()
            WorkflowViewModelStatusKind.CANCELLED -> WorkflowViewModelStatus.cancelled()
            WorkflowViewModelStatusKind.FAILED -> WorkflowViewModelStatus.failed()
            WorkflowViewModelStatusKind.UNKNOWN -> WorkflowViewModelStatus.unknown()
        }

        /**
         * Workflow may never decrease in order but may move between kinds of the same order
         */
        private fun getStatusOrderByKind(kind: WorkflowViewModelStatusKind): Int = when (kind) {
            WorkflowViewModelStatusKind.UNKNOWN -> -1
            WorkflowViewModelStatusKind.NOT_STARTED -> 0
            WorkflowViewModelStatusKind.BOOTING -> 1
            WorkflowViewModelStatusKind.PLANNING -> 2
            WorkflowViewModelStatusKind.DEVELOPING -> 3
            WorkflowViewModelStatusKind.TESTING -> 3
            WorkflowViewModelStatusKind.DEPLOYING -> 3
            WorkflowViewModelStatusKind.FINISHING -> 4
            WorkflowViewModelStatusKind.SUCCEEDED -> 5
            WorkflowViewModelStatusKind.FAILED -> 5
            WorkflowViewModelStatusKind.CANCELLED -> 5
        }
    }
}