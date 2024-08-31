package com.arbr.api_server_base.service.github

import com.arbr.api.github.core.GitHubCommitInfo
import com.arbr.api.github.core.GitHubPullRequestInfo
import com.arbr.api.workflow.event.event_data.CommitEventData
import com.arbr.api.workflow.event.event_data.common.DiffStat
import com.arbr.object_model.functions.external.github.GitHubProjectFunctions
import com.arbr.platform.object_graph.core.resource.field.ArbrCommitCommitMessageValue
import com.arbr.platform.object_graph.core.resource.field.ArbrCommitDiffSummaryValue
import com.arbr.platform.object_graph.file_system.VolumeState
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class GitHubProjectFunctionsImpl: GitHubProjectFunctions {

    /**
     * Via GitHubProjectClient
     */

    override fun commit(
        volumeState: VolumeState,
        commitMessage: String,
    ): Mono<GitHubCommitInfo> { TODO() }

    /**
     * Get the raw numstat text of a commit with the given hash.
     */
    override fun getCommitShowNumStatText(
        volumeState: VolumeState,
        commitHash: String,
    ): Mono<String> { TODO() }

    /**
     * Get the raw name-status text of a commit with the given hash.
     */
    override fun getCommitShowNameStatusText(
        volumeState: VolumeState,
        commitHash: String,
    ): Mono<String> { TODO() }

    override fun getCurrentBranch(
        volumeState: VolumeState,
    ): Mono<String> { TODO() }

    override fun checkoutBaseBranch(
        volumeState: VolumeState,
        branchName: String,
    ): Mono<Void> { TODO() }

    override fun checkoutNewBranch(
        volumeState: VolumeState,
        branchName: String,
    ): Mono<Void> { TODO() }

    override fun pushUpstreamOrigin(
        volumeState: VolumeState,
        branchName: String,
    ): Mono<Void> { TODO() }

    /**
     * Push the current branch to remote origin.
     */
    override fun pushStraight(
        volumeState: VolumeState,
    ): Mono<Void> { TODO() }

    override fun diffBetween(
        volumeState: VolumeState,
        commitShaHead: String,
        commitShaParent: String,
    ): Mono<String> { TODO() }

    /**
     * From GitHubDocumentRepository
     */

    override fun cloneProjectIfNotExists(
        volumeState: VolumeState,
        repoFullName: String
    ): Mono<Void> { TODO() }

    override fun openPullRequest(
        projectName: String,
        head: String,
        prTitle: String,
        prBody: String,
    ): Mono<GitHubPullRequestInfo> { TODO() }

    /**
     * From GitHubProjectService
     */

    override fun getCommitDiffStat(
        volumeState: VolumeState,
        commitHash: String,
    ): Mono<DiffStat> { TODO() }

    override fun commitAndGetCommitEventData(
        volumeState: VolumeState,
        commitMessage: ArbrCommitCommitMessageValue,
        diffSummary: ArbrCommitDiffSummaryValue?,
    ): Mono<CommitEventData> { TODO() }
}
