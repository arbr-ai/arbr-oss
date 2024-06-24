package com.arbr.core_web_dev.workflow.model

import org.slf4j.LoggerFactory

enum class FileOperation(val stringForm: String) {
    CREATE_FILE("create_file"),
    EDIT_FILE("edit_file"),
    DELETE_FILE("delete_file");

    companion object {
        private val logger = LoggerFactory.getLogger(FileOperation::class.java)

        fun parseLine(line: String): FileOperation? {
            // Parse as leniently as possible
            val tokens = line
                .split(Regex("[\\s\\n\\(\\)\\[\\],\\:\"\']+"))
                .filter { it.isNotBlank() }
            var op: FileOperation? = null
            for (token in tokens) {
                if (token.lowercase() in listOf("create", "create_file")) {
                    op = CREATE_FILE
                } else if (token.lowercase() in listOf("edit", "edit_file")) {
                    op = EDIT_FILE
                } else if (token.lowercase() in listOf("delete", "delete_file")) {
                    op = DELETE_FILE
                }
            }

            if (op == null) {
                logger.error("Invalid file op: $line")
                return null
            }

            return op
        }
    }
}