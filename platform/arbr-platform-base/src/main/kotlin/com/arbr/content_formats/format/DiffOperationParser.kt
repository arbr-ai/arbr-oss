package com.arbr.content_formats.format

import kotlin.math.max

sealed interface Either<S, T> {
    data class Left<S, T>(val value: S): Either<S, T>
    data class Right<S, T>(val value: T): Either<S, T>

    fun <V> mapEither(mapLeft: (S) -> V, mapRight: (T) -> V): V {
        return when (this) {
            is Left -> mapLeft(this.value)
            is Right -> mapRight(this.value)
        }
    }

    fun leftOrNull(): S? = mapEither({ it }, { null })
    fun rightOrNull(): T? = mapEither({ null }, { it })
}

class DiffOperationParser {

    sealed class DiffPatchSpecialLine {
        object FilePlus : DiffPatchSpecialLine()
        object FileMinus : DiffPatchSpecialLine()
        data class SectionRange(
            val sourceLineStart: Int,
            val sourceRangeSize: Int,
            val targetLineStart: Int,
            val targetRangeSize: Int
        ) : DiffPatchSpecialLine() {
            companion object {
                private val sectionHeaderRegex = Regex("@@ -(\\d+),(\\d+) \\+(\\d+),(\\d+) @@.*")

                fun parse(line: String): SectionRange? {
                    val match = sectionHeaderRegex.matchEntire(line)
                    val groupValues = match?.groupValues ?: return null
                    if (groupValues.size < 5) {
                        return null
                    }

                    // Line numbers seem to be 1-indexed - account for that here
                    return SectionRange(
                        max(0, groupValues[1].toInt() - 1),
                        groupValues[2].toInt(),
                        max(0, groupValues[3].toInt() - 1),
                        groupValues[4].toInt(),
                    )
                }
            }
        }

        object DiffHeader : DiffPatchSpecialLine()
        object IndexHeader : DiffPatchSpecialLine()
        object Comment : DiffPatchSpecialLine()

        companion object {
            fun match(lineText: String): DiffPatchSpecialLine? {
                val lineStart = DiffPatchSpecialLineStart.match(lineText) ?: return null
                return when (lineStart) {
                    DiffPatchSpecialLineStart.FILE_PLUS -> FilePlus
                    DiffPatchSpecialLineStart.FILE_MINUS -> FileMinus
                    DiffPatchSpecialLineStart.SECTION_RANGE -> {
                        SectionRange.parse(lineText)
                    }

                    DiffPatchSpecialLineStart.DIFF_HEADER -> DiffHeader
                    DiffPatchSpecialLineStart.INDEX_HEADER -> IndexHeader
                    DiffPatchSpecialLineStart.COMMENT -> Comment
                }
            }
        }
    }

    enum class DiffPatchSpecialLineStart(val lineStart: String) {
        FILE_PLUS("+++"),
        FILE_MINUS("---"),
        SECTION_RANGE("@@"),
        DIFF_HEADER("diff"),
        INDEX_HEADER("index"),
        COMMENT("\\");

        companion object {
            fun match(line: String): DiffPatchSpecialLineStart? {
                return values().firstOrNull {
                    line.startsWith(it.lineStart)
                }
            }
        }
    }

    fun parse(lineNumber: Int, line: String): Either<DiffOperation, DiffPatchSpecialLine> {
        val specialMatch = DiffPatchSpecialLine.match(line)
        if (specialMatch != null) {
            return Either.Right(specialMatch)
        }

        val leading = line.take(1)
        val operationKind = when (leading) {
            "+" -> DiffOperationKind.ADD
            "-" -> DiffOperationKind.DEL
            " ", "" -> DiffOperationKind.NOP // Special case for empty lines - treat them as NOPs since they appear frequently
            else -> DiffOperationKind.NUL
        }

        // TODO leading space alignment?
        val content = line.drop(1)
        val operation = DiffOperation(
            operationKind,
            content,
            lineNumber,
        )
        return Either.Left(operation)
    }
}