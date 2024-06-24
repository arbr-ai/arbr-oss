package com.arbr.api_server_base.service.github

//import com.arbr.api.github.core.GitHubPullRequestInfo
//import com.arbr.api_server_base.service.github.client.GitHubClient
//import com.arbr.api_server_base.service.github.client.GitHubClientFactory
//import com.arbr.og_engine.file_system.VolumeState
//import com.arbr.util_common.reactor.single
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//
//@Component
//final class GitHubDocumentRepository(
//    private val gitHubClientFactory: GitHubClientFactory,
//) {
//    private val logger = LoggerFactory.getLogger(GitHubDocumentRepository::class.java)
//
//    private fun gitHubClient(): Mono<GitHubClient> {
//        return GitHubCredentialsUtils.fromContext().map {
//            // TODO: Cache
//            gitHubClientFactory.client(it.accessToken)
//        }
//            .single("Not logged into GitHub")
//    }
//
//    fun cloneProjectIfNotExists(
//        volumeState: VolumeState,
//        repoFullName: String
//    ): Mono<Void> {
//        return volumeState.cloneProject()
//            .then(
//                volumeState
//                    .execWriteInProject(
//                        """
//                        git reset origin --hard
//                        git pull
//                    """.trimIndent()
//                    )
//            )
//            .then()
//    }
//
//    fun commit(
//        volumeState: VolumeState,
//        message: String,
//    ): Mono<Void> {
//        return volumeState.execReadInProject(
//            """
//                git add -u
//                git add -A
//                git commit -m "$message"
//            """.trimIndent()
//        )
//            .then()
//    }
//
//    fun openPullRequest(
//        projectName: String,
//        head: String,
//        prTitle: String,
//        prBody: String,
//    ): Mono<GitHubPullRequestInfo> {
//        return gitHubClient()
//            .flatMap { client ->
//                client
//                    .getUserRepos()
//                    .flatMapIterable { repoDetailList ->
//                        repoDetailList
//                            .distinctBy { r -> r.fullName }
//                    }
//                    .filter {
//                        it.fullName == projectName
//                    }
//                    .single("No repo $projectName")
//                    .doOnError { logger.error("Repo not found: $projectName", it) }
//                    .flatMap {
//                        client.createPullRequest(
//                            it.fullName,
//                            prTitle,
//                            head,
//                            it.defaultBranch,
//                            prBody,
//                        )
//                    }
//            }
//    }
//}
