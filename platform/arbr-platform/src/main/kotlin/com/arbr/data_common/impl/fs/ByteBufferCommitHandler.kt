package com.arbr.data_common.impl.fs

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

fun interface ByteBufferCommitHandler {
    fun flushByteBuffer(
        cumulativeByteArray: ByteArrayOutputStream,
        byteBuffer: ByteBuffer,
        commit: Boolean,
    )
}