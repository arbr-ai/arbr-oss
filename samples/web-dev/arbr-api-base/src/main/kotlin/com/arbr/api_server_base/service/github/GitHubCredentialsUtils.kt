package com.arbr.api_server_base.service.github

import com.arbr.api.github.core.GitHubCredentials
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object GitHubCredentialsUtils {
    private const val GITHUB_CREDENTIALS_USERNAME = "gitHubCredentialsUsername"
    private const val GITHUB_CREDENTIALS_ACCESS_TOKEN = "gitHubCredentialsAccessToken"

    fun <T> Flux<T>.writeGitHubCredentials(
        username: String,
        accessToken: String,
    ): Flux<T> = this.contextWrite { ctx ->
        ctx
            .put(GITHUB_CREDENTIALS_USERNAME, username)
            .put(GITHUB_CREDENTIALS_ACCESS_TOKEN, accessToken)
    }

    fun <T> Mono<T>.writeGitHubCredentials(
        username: String,
        accessToken: String,
    ): Mono<T> = this.contextWrite { ctx ->
        ctx
            .put(GITHUB_CREDENTIALS_USERNAME, username)
            .put(GITHUB_CREDENTIALS_ACCESS_TOKEN, accessToken)
    }

    fun fromContext(): Mono<GitHubCredentials> {
        return Mono.deferContextual {
            val username = it.get<String>(GITHUB_CREDENTIALS_USERNAME)
            val accessToken = it.get<String>(GITHUB_CREDENTIALS_ACCESS_TOKEN)

            Mono.just(GitHubCredentials(username, accessToken))
        }
    }
}
