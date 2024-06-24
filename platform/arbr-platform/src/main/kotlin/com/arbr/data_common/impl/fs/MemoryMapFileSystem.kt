package com.arbr.data_common.impl.fs

import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.util.regex.Pattern


class MemoryMapFileSystem(
    private val provider: MemoryMapFileSystemProvider,
) : FileSystem() {
    override fun close() {
    }

    override fun provider(): FileSystemProvider {
        return provider
    }

    override fun isOpen(): Boolean {
        return true
    }

    override fun isReadOnly(): Boolean {
        return false
    }

    override fun getSeparator(): String {
        return SEPARATOR
    }

    override fun getRootDirectories(): MutableIterable<Path> {
        return mutableListOf(
            Paths.get("")
        )
    }

    override fun getFileStores(): MutableIterable<FileStore> {
        return mutableListOf()
    }

    override fun supportedFileAttributeViews(): MutableSet<String> {
        return mutableSetOf()
    }

    override fun getPath(first: String, vararg more: String?): Path {
        val tokens = first.split(SEPARATOR) + more.filterNotNull().flatMap {
            it.split(SEPARATOR)
        }
        val pathString = tokens.joinToString("/")

        return MemoryMapPath(this, pathString.toByteArray(StandardCharsets.UTF_8))
    }

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        val colonIndex = syntaxAndPattern.indexOf(':')
        require(!(colonIndex <= 0 || colonIndex == syntaxAndPattern.length - 1)) { "syntaxAndPattern must have form \"syntax:pattern\" but was \"$syntaxAndPattern\"" }

        val syntax = syntaxAndPattern.substring(0, colonIndex)
        val pattern = syntaxAndPattern.substring(colonIndex + 1)
        val expr: String
        when (syntax) {
            "regex" -> expr = pattern
            else -> throw UnsupportedOperationException("Unsupported syntax \'$syntax\'")
        }
        val regex: Pattern = Pattern.compile(expr)
        return PathMatcher { path -> regex.matcher(path.toString()).matches() }
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        throw UnsupportedOperationException()
    }

    override fun newWatchService(): WatchService {
        throw UnsupportedOperationException()
    }

    companion object {
        const val SEPARATOR = "/"
    }
}