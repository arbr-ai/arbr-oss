package com.arbr.codegen.util

import com.github.mustachejava.MustacheResolver
import java.io.FileInputStream
import java.io.Reader
import java.nio.file.Path
import java.nio.file.Paths

class SimplePathResolver(
    private val paths: List<Path>,
) : MustacheResolver {
    init {
        if (paths.isEmpty()) {
            throw Exception("SimplePathResolver has no resources")
        }
    }

    override fun getReader(resourceName: String): Reader {
        if (resourceName.isBlank()) {
            throw Exception("Blank resource name")
        }

        val resourcePath = Paths.get(resourceName)

        val (bestMatch, matchSize) = paths.maxOfWith(
            { o1, o2 -> o1.second - o2.second },
        ) { path ->
            path to (PathUtils.commonSuffix(listOf(path, resourcePath))?.nameCount ?: 0)
        }

        if (matchSize <= 0) {
            throw Exception("No resource match for $resourceName - Paths: ${paths.joinToString(", ") { it.toString() }}")
        }

        return FileInputStream(bestMatch.toFile()).bufferedReader()
    }
}