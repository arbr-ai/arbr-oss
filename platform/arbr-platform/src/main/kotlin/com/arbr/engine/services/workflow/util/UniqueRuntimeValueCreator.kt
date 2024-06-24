package com.arbr.engine.services.workflow.util

import com.arbr.util_common.hashing.HashUtils
import java.util.*

object UniqueRuntimeValueCreator {

    private val uniqueRuntimeValue: String by lazy {
        HashUtils.sha1Hash(UUID.randomUUID().toString()).takeLast(8)
    }

    fun branchName(
        workflowHandleId: String,
        workflowId: String,
    ): String {
        val idHash = HashUtils.sha1Hash(workflowId).takeLast(8)
        return "arbr-${idHash}-${workflowHandleId}-$uniqueRuntimeValue"
    }
}