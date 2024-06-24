package com.arbr.api_server_base.service.file_system.client

import com.arbr.api_server_base.service.file_system.model.GitRemoteVolumeState
import com.arbr.api_server_base.service.git.client.GitWebClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class VolumeStateFactory(
    @Value("\${topdown.git.base_url}")
    private val gitWebserverBaseUrl: String,
) {

    fun gitRemoteVolumeState(
        projectFullName: String,
        workflowHandleId: String,
    ): GitRemoteVolumeState {
        val gitWebClient = GitWebClient(
            workflowHandleId,
            gitWebserverBaseUrl
        )
        return GitRemoteVolumeState(workflowHandleId, projectFullName, gitWebClient)
    }
}
