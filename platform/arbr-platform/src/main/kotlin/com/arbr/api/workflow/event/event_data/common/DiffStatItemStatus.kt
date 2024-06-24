package com.arbr.api.workflow.event.event_data.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * https://git-scm.com/docs/git-diff
 *
 * Possible status letters are:
 * A: addition of a file
 * C: copy of a file into a new one
 * D: deletion of a file
 * M: modification of the contents or mode of a file
 * R: renaming of a file
 * T: change in the type of the file (regular file, symbolic link or submodule)
 * U: file is unmerged (you must complete the merge before it can be committed)
 * X: "unknown" change type (most probably a bug, please report it)
 */
enum class DiffStatItemStatus(
    @JsonValue
    val serializedName: String,
    private val code: String,
) {
    ADDED("added", "A"),
    COPIED("copied", "C"),
    DELETED("deleted", "D"),
    MODIFIED("modified", "M"),
    RENAMED("renamed", "R"),
    CHANGED_TYPE("changed_type", "T"),
    UNMERGED("unmerged", "U"),
    UNKNOWN("unknown", "X");

    companion object {

        /**
         * Parse a diff status value leniently, matching either the full name or the status code.
         */
        fun parseLenient(value: String): DiffStatItemStatus {
            val lowerValue = value.lowercase()
            val upperValue = value.uppercase()
            return values().firstOrNull {
                it.code == upperValue || it.serializedName == lowerValue
            } ?: UNKNOWN
        }

        /**
         * Parse from JSON.
         */
        @JsonCreator
        @JvmStatic
        fun create(
            value: String
        ): DiffStatItemStatus {
            return parseLenient(value)
        }
    }
}