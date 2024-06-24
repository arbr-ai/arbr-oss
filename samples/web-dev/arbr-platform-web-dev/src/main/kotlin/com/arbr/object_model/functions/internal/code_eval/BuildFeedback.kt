package com.arbr.object_model.functions.internal.code_eval

data class BuildFeedback(
    val success: Boolean,

    /**
     * A list of error messages from the build attempt.
     */
    val failures: List<String>
)
