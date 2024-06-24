package com.arbr.codegen.util

import java.nio.file.Path
import kotlin.io.path.relativeTo
import kotlin.math.min

object PathUtils {

    fun commonRoot(paths: List<Path>, relativeTo: Path): Path? {
        val firstPath = paths.firstOrNull() ?: return null
        // Compute common path root
        var commonPath = firstPath.relativeTo(relativeTo)
        for (path in paths) {
            val filePath = path.relativeTo(relativeTo)
            for (i in 0 until min(commonPath.nameCount, filePath.nameCount)) {
                if (commonPath.getName(i) != filePath.getName(i)) {
                    commonPath = commonPath.subpath(0, i)
                    break
                }
            }
        }

        return commonPath
    }

    fun commonSuffix(paths: List<Path>): Path? {
        val firstPath = paths.firstOrNull() ?: return null
        // Compute common path suffix
        val firstPathCount = firstPath.nameCount
        var startIndex = firstPathCount

        for (path in paths) {
            val minLength = min(firstPathCount, path.nameCount)
            var commonSuffixStartIndex = firstPathCount

            for (backwardsI in 1 until minLength) {
                if (firstPath.getName(firstPathCount - backwardsI) != path.getName(path.nameCount - backwardsI)) {
                    break
                }
                commonSuffixStartIndex--
            }
            startIndex = commonSuffixStartIndex
        }

        return if (startIndex == firstPath.nameCount) {
            null
        } else {
            firstPath.subpath(startIndex, firstPathCount)
        }
    }
}
