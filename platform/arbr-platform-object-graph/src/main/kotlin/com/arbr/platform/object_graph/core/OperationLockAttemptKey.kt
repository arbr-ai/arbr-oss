package com.arbr.platform.object_graph.core

import com.arbr.platform.object_graph.util.LexIntSequence

data class OperationLockAttemptKey(
    val ordinalSeq: LexIntSequence
)