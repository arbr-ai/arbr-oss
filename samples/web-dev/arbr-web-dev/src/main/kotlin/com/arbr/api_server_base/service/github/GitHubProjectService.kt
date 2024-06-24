package com.arbr.api_server_base.service.github

//import com.arbr.api.workflow.event.event_data.CommitEventData
//import com.arbr.api.workflow.event.event_data.common.DiffStat
//import com.arbr.api.workflow.event.event_data.common.DiffStatItem
//import com.arbr.api.workflow.event.event_data.common.DiffStatItemStatus
//import com.arbr.api_server_base.service.github.client.GitHubProjectClient
//import com.arbr.object_model.core.resource.field.ArbrCommitCommitMessageValue
//import com.arbr.object_model.core.resource.field.ArbrCommitDiffSummaryValue
//import com.arbr.og_engine.file_system.VolumeState
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//import reactor.kotlin.core.util.function.component1
//import reactor.kotlin.core.util.function.component2
//
///**
// * Service for managing GitHub projects with higher-level concepts than the Client (such as diff parsing).
// */
//@Component
//class GitHubProjectService(
//    private val gitHubProjectClient: GitHubProjectClient,
//) {
//
//    private fun parseGitStatOutput(gitStatOutput: String, gitNameStatusOutput: String): DiffStat {
//        // First, parse the --name-status output into a map for easy lookup
//        val statusMap = gitNameStatusOutput.lineSequence()
//            .filter { it.isNotBlank() } // Filter out blank lines
//            .mapNotNull { line ->
//                val parts = whitespaceSplitRegex.split(line, limit = 2)
//
//                if (parts.size == 2) {
//                    val status = parts[0]
//                    val filePath = parts[1]
//
//                    // Map file path to its status
//                    filePath to DiffStatItemStatus.parseLenient(status)
//                } else {
//                    null
//                }
//            }
//            .toMap()
//
//        // Then, parse the --stat output while looking up the status from the map
//        val diffStatItems = gitStatOutput.lineSequence()
//            .filter { it.isNotBlank() } // Filter out blank lines
//            .mapNotNull { line ->
//                val match = numStatLineRegex.matchEntire(line.trim())
//                if (match == null) {
//                    null
//                } else {
//                    val additions = match.groups[1]?.value?.toLongOrNull()
//                    val deletions = match.groups[2]?.value?.toLongOrNull()
//                    val filePath = match.groups[3]?.value
//
//                    if (additions == null || deletions == null || filePath == null) {
//                        null
//                    } else {
//                        // Look up the file status, defaulting to UNKNOWN if not found (which shouldn't happen)
//                        val status = statusMap[filePath] ?: DiffStatItemStatus.UNKNOWN
//
//                        DiffStatItem(status, filePath, additions, deletions)
//                    }
//                }
//            }.toList()
//
//        return DiffStat(diffStatItems)
//    }
//
//    fun getCommitDiffStat(
//        volumeState: VolumeState,
//        commitHash: String,
//    ): Mono<DiffStat> {
//        return Mono.zip(
//            gitHubProjectClient.getCommitShowNumStatText(volumeState, commitHash),
//            gitHubProjectClient.getCommitShowNameStatusText(volumeState, commitHash),
//        ).map { (statText, nameStatusText) ->
//            parseGitStatOutput(statText, nameStatusText)
//        }
//    }
//
//    fun commitAndGetCommitEventData(
//        volumeState: VolumeState,
//        commitMessage: ArbrCommitCommitMessageValue,
//        diffSummary: ArbrCommitDiffSummaryValue?,
//    ): Mono<CommitEventData> {
//        return gitHubProjectClient
//            .commit(volumeState, commitMessage.value)
//            .flatMap { commitInfo ->
//                getCommitDiffStat(volumeState, commitInfo.commitHash)
//                    .single()
//                    .map { diffStat ->
//                        CommitEventData(
//                            commitInfo.commitHash,
//                            commitInfo.message,
//                            commitInfo.commitUrl,
//                            diffSummary?.value,
//                            diffStat,
//                        )
//                    }
//                    .onErrorResume {
//                        logger.warn("Failed to compute commit diffstat; defaulting to null", it)
//                        Mono.just(
//                            CommitEventData(
//                                commitInfo.commitHash,
//                                commitInfo.message,
//                                commitInfo.commitUrl,
//                                diffSummary?.value,
//                                null,
//                            )
//                        )
//                    }
//            }
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(GitHubProjectService::class.java)
//
//        private val whitespaceSplitRegex = Regex("\\s+")
//        private val numStatLineRegex = Regex("^(\\d+)\\s+(\\d+)\\s+(.*)")
//    }
//}