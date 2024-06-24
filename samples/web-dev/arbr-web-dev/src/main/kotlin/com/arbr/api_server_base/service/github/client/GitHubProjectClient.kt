package com.arbr.api_server_base.service.github.client

//import com.arbr.api_server_base.service.github.GitHubDocumentRepository
//import com.arbr.engine.services.workflow.model.CommitInfo
//import com.arbr.og_engine.file_system.VolumeState
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//
//@Component
//class GitHubProjectClient(
//    private val gitHubDocumentRepository: GitHubDocumentRepository,
//) {
//
//    private fun getPushedUrl(
//        volumeState: VolumeState,
//        commitHash: String,
//    ): String {
//        // Ideally should use the URL tied to the PR once it's open, but this URL works for either state
//        val projectFullName = volumeState.getProjectFullName()
//        val url = "https://github.com/$projectFullName/commit/$commitHash"
//        return url
//    }
//
//    fun commit(
//        volumeState: VolumeState,
//        commitMessage: String,
//    ): Mono<CommitInfo> {
//        return volumeState.execReadInProject("git add -AN; git diff HEAD")
//            .flatMap { shout ->
//                if (shout.stderr.any { it.isNotBlank() }) {
//                    Mono.error(Exception(shout.stderr.joinToString("\n")))
//                } else {
//                    Mono.just(shout.stdout.joinToString("\n"))
//                }
//            }
//            .flatMap { commitDiff ->
//                gitHubDocumentRepository.commit(
//                    volumeState,
//                    commitMessage,
//                ).then(
//                    volumeState.execReadInProject(
//                        "git log HEAD...HEAD~1 --format=format:\"%H\""
//                    )
//                        .map { it.stdout.joinToString("\n").trim() }
//                        .map { commitHash ->
//                            CommitInfo(
//                                commitHash,
//                                commitMessage,
//                                getPushedUrl(volumeState, commitHash),
//                                commitDiff,
//                            )
//                        }
//                )
//            }
//    }
//
//    /**
//     * Get the raw numstat text of a commit with the given hash.
//     */
//    fun getCommitShowNumStatText(
//        volumeState: VolumeState,
//        commitHash: String,
//    ): Mono<String> {
//        return volumeState.execReadInProject(
//            "git show --numstat --pretty=format: $commitHash"
//        )
//            .single()
//            .flatMap { shout ->
//                val diff = shout.stdout.joinToString("\n") { it }.trim()
//                if (diff.isBlank()) {
//                    Mono.error(Exception("Empty stat for $commitHash"))
//                } else {
//                    Mono.just(diff)
//                }
//            }
//    }
//
//    /**
//     * Get the raw name-status text of a commit with the given hash.
//     */
//    fun getCommitShowNameStatusText(
//        volumeState: VolumeState,
//        commitHash: String,
//    ): Mono<String> {
//        return volumeState.execReadInProject(
//            "git show --name-status --pretty=format: $commitHash"
//        )
//            .single()
//            .flatMap { shout ->
//                val diff = shout.stdout.joinToString("\n") { it }.trim()
//                if (diff.isBlank()) {
//                    Mono.error(Exception("Empty name-status for $commitHash"))
//                } else {
//                    Mono.just(diff)
//                }
//            }
//    }
//
//    fun getCurrentBranch(
//        volumeState: VolumeState,
//    ): Mono<String> {
//        return volumeState.execReadInProject(
//            "git rev-parse --abbrev-ref HEAD"
//        ).mapNotNull { shout ->
//            shout.stdout.firstOrNull { line ->
//                line.trim().isNotBlank()
//            }
//        }
//    }
//
//    fun checkoutBaseBranch(
//        volumeState: VolumeState,
//        branchName: String,
//    ): Mono<Void> {
//        return volumeState.execWriteInProject(
//            "git checkout $branchName"
//        ).then()
//    }
//
//    fun checkoutNewBranch(
//        volumeState: VolumeState,
//        branchName: String,
//    ): Mono<Void> {
//        return volumeState.execWriteInProject(
//            "git checkout -b $branchName"
//        ).then()
//    }
//
//    fun pushUpstreamOrigin(
//        volumeState: VolumeState,
//        branchName: String,
//    ): Mono<Void> {
//        return volumeState.execReadInProject(
//            "git push --set-upstream origin $branchName"
//        ).then()
//    }
//
//    /**
//     * Push the current branch to remote origin.
//     */
//    fun pushStraight(
//        volumeState: VolumeState,
//    ): Mono<Void> {
//        return volumeState.execReadInProject(
//            "git push"
//        ).then()
//    }
//
//    fun diffBetween(
//        volumeState: VolumeState,
//        commitShaHead: String,
//        commitShaParent: String,
//    ): Mono<String> {
//        return volumeState.execReadInProject("git diff $commitShaParent $commitShaHead")
//            .flatMap { shout ->
//                if (shout.stderr.any { it.isNotBlank() }) {
//                    Mono.error(Exception(shout.stderr.joinToString("\n")))
//                } else {
//                    Mono.just(shout.stdout.joinToString("\n"))
//                }
//            }
//    }
//}