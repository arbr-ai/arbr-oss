package com.arbr.data_common.impl.fs

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.SeekableByteChannel
import kotlin.math.min

class MemoryMapByteChannel(
    private val buffer: ByteBuffer,
    private val commitHandler: ByteBufferCommitHandler,
) : SeekableByteChannel {
    private var position: Long = 0
    private var closed = false

    private val cumulativeByteArray = ByteArrayOutputStream()

    private fun flushBuffer(
        commit: Boolean,
    ) {
        commitHandler.flushByteBuffer(
            cumulativeByteArray,
            buffer,
            commit,
        )
    }

    override fun read(dst: ByteBuffer): Int {
        val l = min(dst.remaining().toLong(), size() - position).toInt()
        dst.put(0, buffer, position.toInt(), l)
        position += l.toLong()
        return l
    }

    override fun write(src: ByteBuffer): Int {
        if (closed) {
            throw ClosedChannelException()
        }

        if (buffer.remaining() <= 0) {
            flushBuffer(false)
        }

        val srcLength = src.remaining()

        // easy case: enough free space in buffer for entire input
        if (buffer.remaining() >= src.remaining()) {
            buffer.put(src)

            flushBuffer(true)
            return srcLength
        }

        // store current limit
        val srcEnd = src.position() + src.remaining()

        while (src.position() + buffer.remaining() <= srcEnd) {
            // fill partial buffer as much as possible and flush
            src.limit(src.position() + buffer.remaining())
            buffer.put(src)
            flushBuffer(false)
        }

        // reset original limit
        src.limit(srcEnd)

        // copy remaining partial block into now-empty buffer
        buffer.put(src)

        flushBuffer(true)
        return srcLength
    }

    override fun position(): Long {
        return position
    }

    override fun position(newPosition: Long): SeekableByteChannel {
        position = newPosition
        return this
    }

    override fun size(): Long {
        return buffer.capacity().toLong()
    }

    override fun truncate(size: Long): SeekableByteChannel {
        val tailLength = size - size()
        buffer.put(size.toInt(), ByteArray(tailLength.toInt()))
        return this
    }

    override fun isOpen(): Boolean {
        return !closed
    }

    override fun close() {
        synchronized(this) {
            if (!closed) {
                closed = true

                buffer.clear()
            }
        }
    }
}