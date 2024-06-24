package com.arbr.api_server_base.app

import com.arbr.api_server_base.service.user.UserService
import com.arbr.engine.services.workflow.setup.WorkflowCredentialsProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class WorkflowCredentialsConfiguration {

    @Bean
    fun workflowCredentialsProvider(
        userService: UserService
    ): WorkflowCredentialsProvider {
        return object : WorkflowCredentialsProvider {
            override fun getUserWorkflowCredentials(userId: Long, workflowId: Long): Mono<Map<String, Any>> {
                return userService.getUserCredentials(userId).map { userWorkflowCredentials ->
                    mutableMapOf<String, Any>().apply {
                        userWorkflowCredentials.arbr?.let {
                            put(ARBR_CREDENTIALS_JWT, it.value)
                        }
                        userWorkflowCredentials.gitHub?.let {
                            put(GITHUB_CREDENTIALS_USERNAME, it.login)
                            put(GITHUB_CREDENTIALS_ACCESS_TOKEN, it.accessToken)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ARBR_CREDENTIALS_JWT = "arbrCredentialsJwt"
        const val GITHUB_CREDENTIALS_USERNAME = "gitHubCredentialsUsername"
        const val GITHUB_CREDENTIALS_ACCESS_TOKEN = "gitHubCredentialsAccessToken"
    }

}
