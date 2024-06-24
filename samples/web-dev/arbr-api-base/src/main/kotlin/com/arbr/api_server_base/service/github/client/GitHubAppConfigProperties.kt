package com.arbr.api_server_base.service.github.client

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "arbr.github-app")
class GitHubAppConfigProperties(
    val apps: List<String>,
)