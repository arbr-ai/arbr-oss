package com.arbr.engine.util

import com.arbr.og_engine.file_system.VolumeState
import reactor.core.publisher.Mono
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.name

/**
 * TODO: Migrate to core web dev
 */
object FileContentUtils {

    /**
     * Known-good extensions
     */
    private val goodExtensions = listOf(
        "css",
        "html",
        "js",
        "json",
        "jsx",
        "md",
        "yml",
    )

    /**
     * Known-bad extensions
     */
    private val badExtensions = listOf(
        "Dockerfile",
        "conf",
        "dockerignore",
        "gitignore",
        "ico",
        "jpg",
        "png",
        "prod_env",
        "svg",
    )

    private fun extensionOrName(filePath: String): String {
        val path = Paths.get(filePath)
        return path.extension.ifEmpty { path.name }
    }

    /**
     * Vaguely check whether a file is something that should be included in search, summaries, etc.
     *
     * TODO: Deduplicate with WorkflowFileWritingArbiter and legitimize file search filtering etc.
     */
    fun fileIsAdmissible(
        volumeState: VolumeState,
        filePath: String,
    ): Mono<Boolean> {
        if (filePath.contains(".gitignore")) {
            return Mono.just(false)
        }

        val ext = extensionOrName(filePath)
        if (ext in badExtensions) {
            return Mono.just(false)
        }

        return volumeState.execReadInProject("git check-ignore $filePath").map { shout ->
            // Check NOT in output
            shout.stdout.none { line ->
                line.contains(filePath)
            }
        }
    }

    /**
     * Whether a file path represents a package.json dependency definition file, such that
     * `WorkflowFileOpDiffPackageJsonImplementationProcessor` should be used in place of the typical implementation
     * processor.
     *
     * Could generalize to a class-based matcher in the future.
     */
    fun fileIsPackageJson(
        filePath: String,
    ): Boolean {
        return filePath.lowercase().endsWith("package.json")
    }

    fun normalizeFileContent(
        contents: String
    ): String {
        // Enforce trailing newline
//        return contents.trimEnd() + "\n"

        // Temporarily disable normalization because segments need to stay in sync with file contents
        // TODO: Incorporate formatting more deliberately
        return contents
    }
}