package com.arbr.object_model.functions.external.github

import com.arbr.api.github.core.GitHubCommitInfo
import com.arbr.api.github.core.GitHubPullRequestInfo
import com.arbr.api.workflow.event.event_data.CommitEventData
import com.arbr.api.workflow.event.event_data.common.DiffStat
import com.arbr.platform.object_graph.core.resource.field.ArbrCommitCommitMessageValue
import com.arbr.platform.object_graph.core.resource.field.ArbrCommitDiffSummaryValue
import com.arbr.platform.object_graph.file_system.VolumeState
import reactor.core.publisher.Mono

/**
 * TODO: Define interfaces within object model
 */
interface GitHubProjectFunctions {

    /**
     * Via GitHubProjectClient
     */

    fun commit(
        volumeState: VolumeState,
        commitMessage: String,
    ): Mono<GitHubCommitInfo>

    /**
     * Get the raw numstat text of a commit with the given hash.
     */
    fun getCommitShowNumStatText(
        volumeState: VolumeState,
        commitHash: String,
    ): Mono<String>

    /**
     * Get the raw name-status text of a commit with the given hash.
     */
    fun getCommitShowNameStatusText(
        volumeState: VolumeState,
        commitHash: String,
    ): Mono<String>

    fun getCurrentBranch(
        volumeState: VolumeState,
    ): Mono<String>

    fun checkoutBaseBranch(
        volumeState: VolumeState,
        branchName: String,
    ): Mono<Void>

    fun checkoutNewBranch(
        volumeState: VolumeState,
        branchName: String,
    ): Mono<Void>

    fun pushUpstreamOrigin(
        volumeState: VolumeState,
        branchName: String,
    ): Mono<Void>

    /**
     * Push the current branch to remote origin.
     */
    fun pushStraight(
        volumeState: VolumeState,
    ): Mono<Void>

    fun diffBetween(
        volumeState: VolumeState,
        commitShaHead: String,
        commitShaParent: String,
    ): Mono<String>

    /**
     * From GitHubDocumentRepository
     */

    fun cloneProjectIfNotExists(
        volumeState: VolumeState,
        repoFullName: String
    ): Mono<Void>

    fun openPullRequest(
        projectName: String,
        head: String,
        prTitle: String,
        prBody: String,
    ): Mono<GitHubPullRequestInfo>

    /**
     * From GitHubProjectService
     */

    fun getCommitDiffStat(
        volumeState: VolumeState,
        commitHash: String,
    ): Mono<DiffStat>

    fun commitAndGetCommitEventData(
        volumeState: VolumeState,
        commitMessage: ArbrCommitCommitMessageValue,
        diffSummary: ArbrCommitDiffSummaryValue?,
    ): Mono<CommitEventData>
}
