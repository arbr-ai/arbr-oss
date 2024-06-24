package com.arbr.platform.autoconfigure.aws.s3

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider

@Configuration
@ConditionalOnMissingBean(AwsCredentials::class)
class AwsCredentialsConfig {

    @Bean
    @ConditionalOnMissingBean(AwsCredentialsProvider::class)
    fun credentialsProvider(): AwsCredentialsProvider {
        // Use default credentials provider if not otherwise supplied
        return DefaultCredentialsProvider.create()
    }

    @Bean
    fun awsCredentials(
        credentialsProvider: AwsCredentialsProvider,
    ): AwsCredentials? {
        return try {
            credentialsProvider.resolveCredentials()
        } catch (e: Exception) {
            logger.error("Failed to load AWS credentials: ${e.message}")
            null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AwsCredentialsConfig::class.java)
    }
}