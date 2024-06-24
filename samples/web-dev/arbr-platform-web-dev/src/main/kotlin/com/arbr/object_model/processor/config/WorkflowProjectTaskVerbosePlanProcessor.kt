package com.arbr.object_model.processor.config

import com.arbr.util.adapt
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.types.ArbrForeignKey
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

/**
 * Listens for the creation of a project task and adds a verbose plan.
 * This is part of planning that is essentially the entrypoint to all workflows:
 * 1. Verbose plan -> publish
 * 2. Breakdown & synthesize -> SubtaskPlans
 * 3. Simplify -> TaskPlan
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.task-verbose-plan", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectTaskVerbosePlanProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrTask, PartialTask,
        ArbrProject, PartialProject,
        ArbrTask, PartialTask,
        >(objectModelParser) {
    private val taskVerbosePlanApplication = promptLibrary.taskVerbosePlanApplication

    override val name: String
        get() = "task-verbose-plan"
    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrTask>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrTask, PartialTask, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.taskVerbosePlan.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val projectFullName = require(readResource.fullName)
        val platform = require(readResource.platform)
        val description = require(readResource.description)
        val projectFiles = require(readResource.files)
        val taskQuery = require(listenResource.taskQuery)

        return { partialObjectGraph ->
            val writeTask = partialObjectGraph.root
            taskVerbosePlanApplication.invoke(
                projectFullName,
                platform,
                description,
                FilePaths.initializeMerged(
                    projectFiles
                        .mapNotNull { it.value.filePath.getLatestValue() }
                        .map { SourcedStruct1(it) }
                ),
                taskQuery,
                artifactSink.adapt()
            ).map { (verbosePlan) ->
                writeTask.apply {
                    taskVerbosePlan = verbosePlan
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
        requireLatest(listenResource.taskVerbosePlan)
    }

}