package com.arbr.data_common.impl.fs

import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Files
import java.util.*
import kotlin.io.path.createDirectory

class MemoryMapFileSystemProviderTest {

    @Test
    fun `uses memory map file system`() {
        val rootUri = URI("memory:///")
        val rootPath = MemoryMapPaths.get(rootUri)

        println(rootPath)
    }

    @Test
    fun `creates items`() {
        val rootUri = URI("memory:///")
        val rootPath = MemoryMapPaths.get(rootUri)

        val dirPath = MemoryMapPaths.get(rootPath.toString(), "subdir_a")
        val dirUri = URI("memory", null, dirPath.toString(), null, null)
        val dirPath2 = MemoryMapPaths.get(dirUri)
        val resPath = dirPath2.createDirectory()

        println(resPath)

        val fPath0 = MemoryMapPaths.get(resPath.toString(), "abc.txt")

        repeat(4) {
            val str = "$it this is a test of the emergency response system\n"
            val p = if (it > 0) Files.readString(fPath0) else ""
            Files.writeString(fPath0, p + str)
        }

        println("=".repeat(32))
        println(Files.readString(fPath0))
    }

    @Test
    fun `creates many and lists`() {
        val rootUri = URI("memory:///")
        val rootPath = MemoryMapPaths.get(rootUri)

        val dirPath = MemoryMapPaths.get(rootPath.toString(), "subdir_a")
        val dirUri = URI("memory", null, dirPath.toString(), null, null)
        val dirPath2 = MemoryMapPaths.get(dirUri)
        val resPath = dirPath2.createDirectory()

        println(resPath)

        repeat(8) {
            val fPath0 = MemoryMapPaths.get(resPath.toString(), "abc_$it.txt")
            val str = "$it ${UUID.randomUUID()}\n"
            Files.writeString(fPath0, str)
        }

        Files.list(resPath).forEach { dp ->
            println(dp)
        }
    }
}
