package com.arbr.platform.object_graph.file_system

/**
 * TODO: Deprecate
 * Cursed
 */
sealed class TaskInstructionDeprecated {
    abstract val filePath: String

    data class CreateFile(
        override val filePath: String
    ): TaskInstructionDeprecated()

    data class EditFile(override val filePath: String): TaskInstructionDeprecated()

    data class DeleteFile(override val filePath: String): TaskInstructionDeprecated()
}
