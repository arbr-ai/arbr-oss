package com.arbr.api_server_base.service.github.client

import com.arbr.api_server_base.service.github.model.GitHubApp
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

@Component
@ComponentScan("com.arbr.content_formats.mapper")
class GitHubClientFactory(
    gitHubApps: List<GitHubApp>,
    @Qualifier("snakeCaseMapper")
    private val mapper: ObjectMapper,
) {

    private val loginClient = GitHubLoginClient(mapper, gitHubApps)

    fun client(
        accessToken: String
    ): GitHubClient {
        return GitHubClient(mapper, accessToken)
    }

    fun loginClient(): GitHubLoginClient {
        return loginClient
    }
}
