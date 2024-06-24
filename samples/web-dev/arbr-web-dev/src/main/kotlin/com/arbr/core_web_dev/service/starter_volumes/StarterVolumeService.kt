package com.arbr.core_web_dev.service.starter_volumes

import com.arbr.core_web_dev.workflow.input.model.ProjectStarter
import com.arbr.og_engine.file_system.StarterProject
import com.arbr.og_engine.file_system.VolumeState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * A class for serving starter volumes.
 */
@Component
class StarterVolumeService {

    /**
     * Get a starter project for the input, as a volume.
     * TODO: Implement pipeline for starter selection
     */
    private fun getStarterProject(
        input: ProjectStarter,
    ): Mono<StarterProject> {
        val projectStarter = StarterProject.WEB_STARTER
        logger.info("Using ${projectStarter.subdir} as starter for ${input.projectShortName}")
        return Mono.just(projectStarter)
    }

    /**
     * Clear the target volume and write contents to it from the specified starter project, like an image.
     */
    fun writeStarterImageToVolume(
        workflowHandleId: String,
        targetVolume: VolumeState,
        projectStarter: ProjectStarter,
    ): Mono<VolumeState> {
        return getStarterProject(projectStarter).flatMap { starterGitHubProject ->
            targetVolume.clear().flatMap { clearedBaseVolume ->
                clearedBaseVolume.copyStarterProject(starterGitHubProject)
                    .thenReturn(clearedBaseVolume)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StarterVolumeService::class.java)
    }
}
