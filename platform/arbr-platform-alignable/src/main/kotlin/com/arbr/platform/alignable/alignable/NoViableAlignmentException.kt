package com.arbr.platform.alignable.alignable

class NoViableAlignmentException(
    private val kind: String,
    private val source: Any?,
    private val target: Any?,
): Exception("No viable alignment ($kind):\nS: ${source}\nT: $target")