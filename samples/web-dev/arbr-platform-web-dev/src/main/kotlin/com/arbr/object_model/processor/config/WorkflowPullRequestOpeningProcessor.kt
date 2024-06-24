package com.arbr.object_model.processor.config

import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.external.github.GitHubProjectFunctions
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import kotlin.collections.component1
import kotlin.collections.component2

@Component
@ConditionalOnProperty(
    prefix = "arbr.processor.pull-request-opening",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class WorkflowPullRequestOpeningProcessor(
    objectModelParser: ObjectModelParser,
    gitHubProjectFunctions: GitHubProjectFunctions,
    promptLibrary: PromptLibrary,
) : WorkflowBasePullRequestOpeningProcessor(
    objectModelParser,
    promptLibrary,
    gitHubProjectFunctions,
) {
    override val name: String
        get() = "pull-request-opening"

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ): (PartialObjectGraph<ArbrTask, PartialTask, ArbrForeignKey>) -> Mono<Void> {
        // For now, as long as the task eval is at least mostly complete, then open a PR
        requireThatValue(writeResource.taskEvals) { taskEvals ->
            taskEvals.any { (_, status) ->
                status.mostlyComplete.getLatestAcceptedValue()?.value == true || status.complete.getLatestAcceptedValue()?.value == true
            }
        }

        return super.prepareUpdate(listenResource, readResource, writeResource, volumeState, artifactSink)
    }
}
