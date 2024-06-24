package com.arbr.util_common.uri

import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Paths
import kotlin.io.path.extension

sealed class UriModel {

    abstract fun toUri(): URI

    abstract fun lenientFileName(): String

    abstract fun lenientFileExtension(): String

    abstract fun lenientExtendPath(vararg pathTokens: String): UriModel

    abstract val scheme: String

    abstract val lenientEffectivePath: String

    data class SchemeSsp(
        override val scheme: String,
        val schemeSpecificPart: String,
    ) : UriModel() {
        private val pathCharacters = Regex("[a-zA-Z0-9-_:\\./]")

        override val lenientEffectivePath: String = schemeSpecificPart
            .split("!") // Archive delimiter
            .last()
            .map { it.toString() }
            .filter { charString ->
                pathCharacters.matches(charString)
            }
            .joinToString("")

        override fun toUri(): URI {
            return URI(scheme, schemeSpecificPart, null)
        }

        override fun lenientFileName(): String {
            return Paths.get(lenientEffectivePath).fileName.toString()
        }

        override fun lenientFileExtension(): String {
            return Paths.get(lenientEffectivePath).extension.lowercase()
        }

        override fun lenientExtendPath(vararg pathTokens: String): UriModel {
            return copy(schemeSpecificPart = Paths.get(schemeSpecificPart, *pathTokens).toString())
        }
    }

    data class SchemePath(
        override val scheme: String,
        val path: String,
    ) : UriModel() {
        override val lenientEffectivePath: String
            get() = path

        override fun toUri(): URI {
            return URI(scheme, null, path, null, null)
        }

        override fun lenientFileName(): String {
            return Paths.get(lenientEffectivePath).fileName.toString()
        }

        override fun lenientFileExtension(): String {
            return Paths.get(lenientEffectivePath).extension.lowercase()
        }

        override fun lenientExtendPath(vararg pathTokens: String): UriModel {
            return copy(path = Paths.get(path, *pathTokens).toString())
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UriModel::class.java)

        fun ofUri(uri: URI): UriModel {
            logger.debug("URI: $uri")

            val scheme = uri.scheme ?: throw URISyntaxException(uri.toString(), "no scheme")
            val uriPath: String? = uri.path
            val uriSchemeSpecificPart: String? = uri.schemeSpecificPart
            val result = if (uriPath != null) {
                SchemePath(scheme, uriPath)
            } else if (uriSchemeSpecificPart != null) {
                SchemeSsp(scheme, uriSchemeSpecificPart)
            } else {
                throw URISyntaxException(uri.toString(), "no path or ssp")
            }

            logger.debug("Effective path: ${result.lenientEffectivePath}")

            return result
        }
    }

}