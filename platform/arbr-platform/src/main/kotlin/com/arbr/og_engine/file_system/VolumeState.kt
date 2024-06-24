package com.arbr.og_engine.file_system

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * An abstraction of the state of a volume of documents (such as a repository)
 */
interface VolumeState {

    /**
     * ID of the associated workflow in progress with which the volume is associated.
     */
    val workflowHandleId: String

    /**
     * Get the full name of the project associated with this volume.
     */
    fun getProjectFullName(): String

    /**
     * List all files in the volume.
     */
    fun listFiles(): Flux<VolumeFile>

    /**
     * List all file paths in the volume.
     */
    fun listFilePaths(): Flux<VolumeFilePath>

    /**
     * Get a file for the given path.
     */
    fun getFileForPath(volumeFilePath: VolumeFilePath): Mono<VolumeFile>

    /**
     * Evolve the volume state by the given instruction, either by emulating or actually effecting the instruction.
     */
    fun evolve(instruction: TaskInstructionDeprecated, contents: String?): Mono<VolumeState>

    /**
     * Evolve the volume state by deleting everything, either by emulating or actually effecting the deletion.
     */
    fun clear(): Mono<VolumeState>

    /**
     * Execute an arbitrary command which does not affect the state of the file system.
     * TODO: Replace with specific operations.
     */
    fun execReadInProject(
        cmd: String,
    ): Mono<ShellOutput>

    /**
     * Execute an arbitrary command which may affect the state of the file system.
     * Include affected relative file paths or null to invalidate all.
     * TODO: Replace with specific operations.
     */
    fun execWriteInProject(
        cmd: String,
        affectedFilePaths: List<String>? = null
    ): Mono<ShellOutput>

    /**
     * Clone
     */
    fun cloneProject(): Mono<Void>

    /**
     * Copy a starter project
     */
     fun copyStarterProject(starterProject: StarterProject): Mono<Void>

    /**
     * Ping the home page via a headless browser.
     */
    fun pingHeadless(
        targetUrl: String,
    ): Mono<HeadlessDocumentResult>
}
