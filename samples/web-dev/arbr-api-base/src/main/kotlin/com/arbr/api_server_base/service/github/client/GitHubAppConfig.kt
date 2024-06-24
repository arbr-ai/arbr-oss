package com.arbr.api_server_base.service.github.client

import com.arbr.api_server_base.service.github.model.GitHubApp
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
@EnableConfigurationProperties(GitHubAppConfigProperties::class)
class GitHubAppConfig(
    private val springEnvironment: Environment,
) {

    private fun getApp(appName: String): GitHubApp {
        val clientId = springEnvironment.getProperty(
            "arbr.github-app.$appName.client-id"
        ) ?: throw IllegalArgumentException("Missing client ID for app $appName")
        val clientSecret = springEnvironment.getProperty(
            "arbr.github-app.$appName.client-secret"
        ) ?: throw IllegalArgumentException("Missing client secret for app $appName")

        return GitHubApp(appName, clientId, clientSecret)
    }

    @Bean
    fun gitHubApps(
        properties: GitHubAppConfigProperties,
    ): List<GitHubApp> {
        return properties.apps.map(this::getApp)
    }

}
