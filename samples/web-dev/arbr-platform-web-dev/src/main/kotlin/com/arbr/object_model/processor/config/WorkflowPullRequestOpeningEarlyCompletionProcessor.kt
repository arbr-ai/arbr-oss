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

@Component
@ConditionalOnProperty(prefix = "arbr.processor.pull-request-opening-early-completion", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowPullRequestOpeningEarlyCompletionProcessor(
    objectModelParser: ObjectModelParser,
    private val gitHubProjectFunctions: GitHubProjectFunctions,
    promptLibrary: PromptLibrary,
) : WorkflowBasePullRequestOpeningProcessor(
    objectModelParser,
    promptLibrary,
    gitHubProjectFunctions,
) {
    override val name: String
        get() = "pull-request-opening-early-completion"

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ): (PartialObjectGraph<ArbrTask, PartialTask, ArbrForeignKey>) -> Mono<Void> {
        // If the task is marked "finished early" due to user intervention, open a PR.
        requireThatValue(writeResource.taskEvals) { taskEvals ->
            taskEvals.any { (_, status) ->
                status.finishedEarly.getLatestAcceptedValue()?.value == true
            }
        }

        return { pog ->
            val parentClosure = super.prepareUpdate(listenResource, readResource, writeResource, volumeState, artifactSink)

            gitHubProjectFunctions.commit(
                volumeState,
                "Commit partial task progress",
            ).flatMap {
                parentClosure(pog)
            }
        }
    }
}
