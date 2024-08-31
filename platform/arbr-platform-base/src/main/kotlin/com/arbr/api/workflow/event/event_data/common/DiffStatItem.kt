package com.arbr.api.workflow.event.event_data.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DiffStatItem(
    /**
     * Status indicator for change to a file.
     */
    val status: DiffStatItemStatus,

    /**
     * Path to the file in the repo.
     */
    val filePath: String,

    /**
     * Lines of coded changed via addition - "green".
     */
    val additions: Long,

    /**
     * Lines of coded changed via deletion - "red".
     */
    val deletions: Long,
)
