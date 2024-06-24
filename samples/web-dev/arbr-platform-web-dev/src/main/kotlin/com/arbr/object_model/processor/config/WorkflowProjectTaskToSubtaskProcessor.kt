package com.arbr.object_model.processor.config

import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialSubtask
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FilePaths
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Listens for the creation of a project task + verbose plan, then generates subtasks.
 * This is part 2 of planning:
 * 1. Verbose plan -> publish
 * 2. Breakdown & synthesize -> SubtaskPlans
 * 3. Simplify -> TaskPlan
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.task-to-subtask", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectTaskToSubtaskProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrTask, PartialTask,
        ArbrProject, PartialProject,
        ArbrTask, PartialTask,
        >(objectModelParser) {
    private val taskBreakdownIterationApplication = promptLibrary.taskBreakdownIterationApplication
    private val taskBreakdownSynthesizeApplication = promptLibrary.taskBreakdownSynthesizeApplication

    override val name: String
        get() = "task-to-subtask"

    override val writeTargetResourceClass: Class<ArbrTask>
        get() = cls()
    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrTask, PartialTask, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.subtasks.items.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val projectFullName = require(readResource.fullName)
        val platform = require(readResource.platform)
        val description = require(readResource.description)
        val projectFiles = require(readResource.files)

        val taskQuery = require(listenResource.taskQuery)
        val taskVerbosePlan = require(listenResource.taskVerbosePlan)

        return { partialObjectGraph ->
            val writeTask = partialObjectGraph.root

            taskBreakdownIterationApplication.invoke(
                projectFullName,
                platform,
                description,
                taskVerbosePlan,
                FilePaths.initializeMerged(
                    projectFiles.mapNotNull { it.value.filePath.getLatestAcceptedValue() }
                        .map { SourcedStruct1(it) }
                ),
                taskQuery,
                artifactSink.adapt(),
            ).flatMap { (subtasksValue) ->
                taskBreakdownSynthesizeApplication.invoke(
                    projectFullName,
                    platform,
                    description,
                    taskVerbosePlan,
                    taskQuery,
                    subtasksValue,
                    artifactSink.adapt(),
                ).map { (subtasks) ->
                    subtasks.containers.map { (subtaskQuery) ->
                        PartialSubtask(
                            partialObjectGraph,
                            UUID.randomUUID().toString(),
                        ).apply {
                            subtask = subtaskQuery
                            parent = PartialRef(listenResource.uuid)
                        }
                    }
                }
            }.map { subtasks ->
                writeTask.apply {
                    this.subtasks = immutableLinkedMapOfPartials(subtasks)
                }
            }.then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrTask,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val subtasks = requireLatestChildren(listenResource.subtasks.items)

        subtasks.forEach { (_, subtask) ->
            requireLatest(subtask.subtask)
        }
    }

}