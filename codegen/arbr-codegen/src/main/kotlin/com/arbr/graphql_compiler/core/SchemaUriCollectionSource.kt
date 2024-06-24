package com.arbr.graphql_compiler.core

import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.outputStream

open class SchemaUriCollectionSource(
    private val uriStream: Stream<URI>,
) : SchemaSource {

    /**
     * Get a (local) file path from which to load file content.
     * For non-file URIs, attempts to download content to a temporary file via a URL connection.
     * TODO: Support auth configuration
     */
    private fun getLocalFilePathForUri(uri: URI): Path {
        val uriPath = uri.toURL().path

        return if (uri.scheme == "file") {
            Paths.get(uriPath)
        } else {
            // Note the URI spec requires '/' as the separator - not platform specific
            val uriFileName = uriPath.split("/").last()
                .let { baseFileName ->
                    // Note: this could potentially fail for atypical URIs like jars which have a `!` delimiter
                    val extension = Paths.get(baseFileName).extension
                    if (extension in graphQLFileExtensions) {
                        // Use provided name
                        baseFileName
                    } else {
                        "$baseFileName.$GRAPHQL_FILE_DEFAULT_EXTENSION"
                    }
                }

            val fileNameWithoutExtension = Paths.get(uriFileName).nameWithoutExtension
            val tempFilePath = Files.createTempFile(
                fileNameWithoutExtension,
                ".graphql"
            )

            logger.info("Writing content from '${uri}' to file '${tempFilePath}'...")
            uri.toURL().openStream().use { input ->
                tempFilePath.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            tempFilePath
        }
    }

    override fun getSchemaPaths(): Stream<Path> {
        return uriStream
            .map(this::getLocalFilePathForUri)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaSource::class.java)

        private const val GRAPHQL_FILE_DEFAULT_EXTENSION = "graphql"
        private val graphQLFileExtensions = listOf(
            GRAPHQL_FILE_DEFAULT_EXTENSION,
            "graphqls",
        )
    }
}