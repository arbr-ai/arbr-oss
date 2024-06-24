package com.arbr.api_server_base.service.file_system.model

import com.arbr.api_server_base.service.git.client.GitWebClient
import com.arbr.engine.util.FileContentUtils
import com.arbr.og_engine.file_system.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Volume state representing an interface to a git repository managed remotely.
 * All operations on the local git repo for the workflow should happen through the associated instance of this class.
 */
class GitRemoteVolumeState(
    override val workflowHandleId: String,
    private val projectFullName: String,
    private val gitWebClient: GitWebClient,
) : VolumeState {

    private fun createOrUpdateFile(filePathStr: String, contents: String?): Mono<Void> {
        return if (contents == null) {
            gitWebClient.touch(
                projectFullName,
                filePathStr,
            )
        } else {
            gitWebClient.write(
                projectFullName,
                filePathStr,
                contents,
            )
        }
            .doOnSubscribe {
                logger.info("Creating or updating file at $filePathStr with ${contents?.length} bytes")
            }
    }

    private fun deleteFile(filePathStr: String): Mono<Void> {
        return gitWebClient.delete(
            projectFullName,
            filePathStr,
        )
            .doOnSubscribe {
                logger.info("Deleting file at $filePathStr")
            }
    }

    private fun listFileRelPathsGit(): Flux<String> =
        gitWebClient.execInProject(
            projectFullName,
            """
                git ls-tree --full-tree --name-only -r HEAD
                git ls-files -o --exclude-standard
            """.trimIndent()
        )
            .flatMapIterable { shout ->
                shout.stdout
            }
            .filter { it.isNotBlank() }

    override fun getProjectFullName(): String {
        return projectFullName
    }

    override fun getFileTreeRepresentation(): Mono<String> {
        return listFileRelPathsGit()
            .collectList()
            .map {
                it.sorted().joinToString("\n")
            }
    }

    private fun readFileContents(fileRelPath: String): Mono<VolumeFile> =
        gitWebClient.read(projectFullName, fileRelPath)
            .map {
                VolumeFile(fileRelPath, it)
            }

    override fun listFiles(): Flux<VolumeFile> {
        val fileReadPrefetch = 4
        return listFileRelPathsGit()
            .concatMap(this::readFileContents, fileReadPrefetch)
    }

    override fun listFilePaths(): Flux<VolumeFilePath> {
        return listFileRelPathsGit()
            .map { fileRelPath ->
                VolumeFilePath(fileRelPath)
            }
    }

    override fun getFileForPath(volumeFilePath: VolumeFilePath): Mono<VolumeFile> {
        return readFileContents(volumeFilePath.filePath)
    }

    private fun normalize(contents: String): String {
        return FileContentUtils.normalizeFileContent(contents)
    }

    override fun evolve(instruction: TaskInstructionDeprecated, contents: String?): Mono<VolumeState> {
        return when (instruction) {
            is TaskInstructionDeprecated.CreateFile -> {
                if (contents == null) {
                    // TODO: Allow
                    logger.error("Tried to create file at ${instruction.filePath} with null contents")
                    Mono.empty()
                } else {
                    createOrUpdateFile(instruction.filePath, normalize(contents))
                }
            }

            is TaskInstructionDeprecated.EditFile -> {
                if (contents == null) {
                    // It would not be unreasonable to interpret this effectively as a delete - but rejecting the op
                    // might be more conservative
                    logger.error("Tried to edit file at ${instruction.filePath} with null contents")
                    Mono.empty()
                } else {
                    createOrUpdateFile(instruction.filePath, normalize(contents))
                }
            }

            is TaskInstructionDeprecated.DeleteFile -> deleteFile(instruction.filePath)
        }
            .thenReturn(this)
    }

    override fun clear(): Mono<VolumeState> {
        return listFilePaths()
            .flatMap {
                gitWebClient.delete(projectFullName, it.filePath)
            }
            .then(Mono.just(this))
    }

    override fun execReadInProject(
        cmd: String,
    ): Mono<ShellOutput> {
        return gitWebClient.execInProject(projectFullName, cmd)
    }

    override fun execWriteInProject(cmd: String, affectedFilePaths: List<String>?): Mono<ShellOutput> {
        return gitWebClient.execInProject(projectFullName, cmd)
    }

    override fun cloneProject(): Mono<Void> = gitWebClient.clone(projectFullName)

    override fun copyStarterProject(
        starterProject: StarterProject,
    ): Mono<Void> = gitWebClient.copyFromStarter(projectFullName, starterProject.subdir)

    override fun pingHeadless(targetUrl: String): Mono<HeadlessDocumentResult> {
        return gitWebClient.pingHeadless(projectFullName, targetUrl)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GitRemoteVolumeState::class.java)
    }
}