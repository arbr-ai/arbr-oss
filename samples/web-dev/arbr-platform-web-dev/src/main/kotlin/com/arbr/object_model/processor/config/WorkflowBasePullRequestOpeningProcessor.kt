package com.arbr.object_model.processor.config

import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.resource.field.*
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.external.github.GitHubProjectFunctions
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.artifact.WorkflowPullRequestArtifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.CommitMessages
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util.adapt
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.function.Tuple3
import reactor.util.function.Tuples

abstract class WorkflowBasePullRequestOpeningProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    private val gitHubProjectFunctions: GitHubProjectFunctions,
) : ArbrResourceFunction<
        ArbrTask, PartialTask,
        ArbrProject, PartialProject,
        ArbrTask, PartialTask,
        >(objectModelParser) {
    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrTask>
        get() = cls()

    private val featurePullRequestDetailsApplication = promptLibrary.featurePullRequestDetailsApplication

    private fun openPullRequest(
        projectName: ArbrProjectFullNameValue,
        platform: ArbrProjectPlatformValue,
        description: ArbrProjectDescriptionValue,
        taskQuery: ArbrTaskTaskQueryValue,
        commitMessages: List<ArbrCommitCommitMessageValue>,
        branchName: ArbrTaskBranchNameValue,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): Mono<Tuple3<ArbrTaskPullRequestTitleValue, ArbrTaskPullRequestBodyValue, ArbrTaskPullRequestHtmlUrlValue>> {
        return gitHubProjectFunctions.pushUpstreamOrigin(volumeState, branchName.value)
            .then(
                featurePullRequestDetailsApplication.invoke(
                    projectName,
                    platform,
                    description,
                    taskQuery,
                    CommitMessages.initializeMerged(commitMessages.map { SourcedStruct1(it) }),
                    artifactSink.adapt(),
                )
                    .flatMap { (prTitle, prBody) ->
                        val prTitleValue = prTitle.value!!

                        gitHubProjectFunctions.openPullRequest(
                            projectName.value,
                            branchName.value,
                            prTitleValue,
                            prBody.value!!,
                        )
                            .doOnNext { gitHubPullRequestInfo ->
                                artifactSink.next(
                                    WorkflowPullRequestArtifact(
                                        gitHubPullRequestInfo.id,
                                        prTitleValue,
                                        gitHubPullRequestInfo.htmlUrl,
                                        gitHubPullRequestInfo.body,
                                        gitHubPullRequestInfo.mergeCommitSha,
                                    )
                                )
                            }
                            .map { gitHubPullRequestInfo ->
                                Tuples.of(
                                    prTitle,
                                    prBody,
                                    ArbrTask.PullRequestHtmlUrl.materialized(
                                        gitHubPullRequestInfo.htmlUrl
                                    ),
                                )
                            }
                    }
            )
    }

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrProject,
        writeResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ): (PartialObjectGraph<ArbrTask, PartialTask, ArbrForeignKey>) -> Mono<Void> {
        if (
            writeResource.pullRequestTitle.getLatestValue() != null
            && writeResource.pullRequestBody.getLatestValue() != null
            && writeResource.pullRequestHtmlUrl.getLatestValue() != null
        ) {
            // Already done
            throw OperationCompleteException()
        }

        val branchName = require(writeResource.branchName)

        val projectName = require(readResource.fullName)
        val platform = require(readResource.platform)
        val description = require(readResource.description)
        val taskQuery = require(writeResource.taskQuery)
        val commitMessages =
            require(writeResource.subtasks).flatMap { (_, subtask) ->
                require(subtask.commits)
                    .map { (_, commit) -> require(commit.commitMessage) } // Should be hash?
            }

        return { partialObjectGraph ->
            val writeTask = partialObjectGraph.root

            openPullRequest(
                projectName,
                platform,
                description,
                taskQuery,
                commitMessages,
                branchName,
                volumeState,
                artifactSink,
            ).map { (title, body, htmlUrl) ->
                logger.info("Opening pull request: $title")

                writeTask.apply {
                    pullRequestTitle = title
                    pullRequestBody = body
                    pullRequestHtmlUrl = htmlUrl
                }
            }.then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrTask,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ) {
        requireLatest(writeTargetResource.pullRequestTitle)
        requireLatest(writeTargetResource.pullRequestBody)
        requireLatest(writeTargetResource.pullRequestHtmlUrl)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowBasePullRequestOpeningProcessor::class.java)
    }
}
