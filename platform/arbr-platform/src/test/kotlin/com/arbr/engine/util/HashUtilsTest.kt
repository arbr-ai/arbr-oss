package com.arbr.engine.util

import com.arbr.util_common.hashing.HashUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.*
import kotlin.io.path.extension

class HashUtilsTest {

    private val tmpPath = Paths.get("tmp")
    private val filePrefix = "hash_utils_test_"

    @BeforeEach
    fun init() {
        if (!Files.exists(tmpPath)) {
            Files.createDirectories(tmpPath)
        }
    }

    @AfterEach
    fun cleanUp() {
        Files.list(tmpPath)
            .forEach {
                if (it.fileName.toString().startsWith(filePrefix)) {
                    it.toFile().delete()
                }
            }
    }

    private fun writeToFile(content: String): File {
        val path = Paths.get(tmpPath.toString(), filePrefix + UUID.randomUUID().toString().takeLast(8))
        val file = path.toFile()
        file.createNewFile()
        file.writeText(content)
        return file
    }

    @Test
    fun `computes sha256 hash`() {
        val content = "This is a test"
        val file = writeToFile(content)
        val fileText = file.readText()
        Assertions.assertEquals(content, fileText)

        val fileBasedHash = HashUtils.sha256Hash(file.inputStream())

        val fileStringBasedHash = HashUtils.sha256Hash(fileText)
        Assertions.assertEquals(fileStringBasedHash, fileBasedHash)

        val stringBasedHash = HashUtils.sha256Hash(content)
        Assertions.assertEquals(stringBasedHash, fileBasedHash)
    }

    @Test
    fun `computes sha256 hash of big file`() {
        val file = Files.list(tmpPath)
            .filter { it.extension == "jsx" }
            .findFirst()
            .get()
            .toFile()

        val fileText = file.readText()

        val fileBasedHash = HashUtils.sha256Hash(file.inputStream())

        val fileStringBasedHash = HashUtils.sha256Hash(fileText)
        Assertions.assertEquals(fileStringBasedHash, fileBasedHash)
    }

    private fun timed(f: () -> Unit): Long {
        val startNanos = System.nanoTime()
        f()
        val endNanos = System.nanoTime()
        return Duration.ofNanos(endNanos - startNanos).toNanos()
    }

    @Test
    fun `computes sha256 hash of big file timed`() {
        val file = Files.list(tmpPath)
            .filter { it.extension == "jsx" }
            .findFirst()
            .get()
            .toFile()

        val fileText = file.readText()

        var totalMs = 0.0
        repeat(100) {
            totalMs += timed {
                HashUtils.sha256Hash(fileText)
            }
        }
        println("Text: ${totalMs / 100.0}ns")

        for (bufferSize in listOf(16, 64, 128, 268, 1024, 2048)) {
            totalMs = 0.0
            repeat(100) {
                val fis = file.inputStream()
                totalMs += timed {
                    HashUtils.sha256Hash(fis, bufferSize)
                }
            }
            println("FIS[$bufferSize]: ${totalMs / 100.0}ns")
        }
    }

}