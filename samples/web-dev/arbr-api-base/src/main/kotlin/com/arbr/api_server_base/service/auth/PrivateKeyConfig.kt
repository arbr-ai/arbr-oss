package com.arbr.api_server_base.service.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PrivateKeyConfig(
    @Value("\${arbr.user_auth.private_key}")
    private val userAuthPrivateKey: String,
    @Value("\${topdown.github_auth.private_key}")
    private val githubAuthPrivateKey: String,
) {
    @Bean
    fun privateKeys(): PrivateKeys {
        return PrivateKeys(userAuthPrivateKey, githubAuthPrivateKey)
    }
}
