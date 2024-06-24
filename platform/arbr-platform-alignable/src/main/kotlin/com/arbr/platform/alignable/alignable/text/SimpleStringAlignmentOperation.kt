package com.arbr.platform.alignable.alignable.text

import com.arbr.platform.alignable.alignable.collections.SequenceAlignmentOperation
import com.arbr.platform.alignable.alignable.SwapAlignable

data class SimpleStringAlignmentOperation(
    val charSequenceOperation: SequenceAlignmentOperation<SwapAlignable<Char>, Char>
)