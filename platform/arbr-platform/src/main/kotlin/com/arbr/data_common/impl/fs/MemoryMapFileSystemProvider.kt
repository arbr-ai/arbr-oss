package com.arbr.data_common.impl.fs

import com.arbr.util_common.uri.UriModel
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider


class MemoryMapFileSystemProvider : FileSystemProvider() {

    private val fileSystems = mutableMapOf<String, MemoryMapFileSystem>()
    private val directories = mutableSetOf<String>()
    private val inMemoryMap = mutableMapOf<String, ByteBuffer>()
    private val committedMemoryMap = mutableMapOf<String, String>()

    private fun loadContent(path: String): ByteBuffer {
        return synchronized(this) {
            val committedString = committedMemoryMap[path]

            if (committedString == null) {
                inMemoryMap.computeIfAbsent(path) { _ ->
                    ByteBuffer.allocate(4)
                }
            } else {
                val buffer = ByteBuffer.wrap(committedString.toByteArray())
                inMemoryMap[path] = buffer
                buffer
            }
        }
    }

    private fun flushByteBuffer(
        cumulativeByteArray: ByteArrayOutputStream,
        byteBuffer: ByteBuffer,
        commit: Boolean,
        path: Path,
        options: Set<OpenOption>,
        vararg attrs: FileAttribute<*>,
    ) {
        synchronized(this) {
            val pathString = path.toString()
            val byteArray = byteBuffer.array().sliceArray(0..<byteBuffer.position())

            cumulativeByteArray.writeBytes(byteArray)
            byteBuffer.clear()

            if (commit) {
                val stringValue = String(cumulativeByteArray.toByteArray())
                committedMemoryMap[pathString] = stringValue
            }
        }
    }

    private fun newByteChannelInner(
        path: Path,
        options: Set<OpenOption>,
        vararg attrs: FileAttribute<*>,
    ): SeekableByteChannel? {
        val buffer = synchronized(this) {
            val pathString = path.toString()
            if (pathString in directories) {
                return null
            }

            loadContent(pathString)
        }

        return MemoryMapByteChannel(buffer) { bos, bb, commit ->
            flushByteBuffer(
                bos,
                bb,
                commit,
                path,
                options,
                *attrs,
            )
        }
    }

    override fun getScheme(): String {
        return "memory"
    }

    private fun fsKey(uri: URI): String {
        return uri.schemeSpecificPart
    }

    override fun newFileSystem(uri: URI, env: MutableMap<String, *>?): FileSystem {
        return synchronized(this) {
            fileSystems.compute(fsKey(uri)) { _, _ ->
                MemoryMapFileSystem(this)
            }!!
        }
    }

    override fun getFileSystem(uri: URI): FileSystem {
        return synchronized(this) {
            fileSystems.computeIfAbsent(fsKey(uri)) { _ ->
                MemoryMapFileSystem(this)
            }
        }
    }

    override fun getPath(uri: URI): Path {
        val uriModel = UriModel.ofUri(uri)
        return getFileSystem(uri).getPath(uriModel.lenientEffectivePath)
    }

    override fun newByteChannel(
        path: Path,
        options: MutableSet<out OpenOption>,
        vararg attrs: FileAttribute<*>
    ): SeekableByteChannel {
        return newByteChannelInner(path, options, *attrs) ?: throw IllegalArgumentException()
    }

    @Synchronized
    override fun newDirectoryStream(dir: Path, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> {
        val pathString = dir.toString()
        if (pathString in inMemoryMap) {
            throw IllegalArgumentException()
        }

        val content = loadContent(pathString)
        if (content is Map<*, *>) {
            throw IllegalArgumentException()
        }

        val fs = getFileSystem(dir.toUri()) as MemoryMapFileSystem

        val dirNameCount = dir.nameCount

        val fileChildren = inMemoryMap.keys
            .map { Paths.get(it) }
            .filter { it.nameCount == dirNameCount + 1 }
            .map { it.toString() }
            .filter { it.startsWith(pathString) }

        val dirStringsIterator = fileChildren.iterator()

        return object : DirectoryStream<Path> {
            override fun iterator(): MutableIterator<Path> {
                return object : MutableIterator<Path> {
                    val delegate = dirStringsIterator
                    override fun hasNext(): Boolean {
                        return delegate.hasNext()
                    }

                    override fun next(): Path {
                        val dirString = delegate.next()
                        return MemoryMapPath(
                            fs,
                            dirString.toByteArray(StandardCharsets.UTF_8)
                        )
                    }

                    override fun remove() {
                        throw java.lang.UnsupportedOperationException()
                    }
                }
            }

            override fun close() {
            }
        }
    }

    override fun createDirectory(dir: Path, vararg attrs: FileAttribute<*>?) {
        synchronized(this) {
            val pathTokens = dir.toList()
            val prefixStrings = pathTokens.indices.map { i ->
                val prefixTokens = pathTokens.take(i + 1)
                Paths.get(
                    prefixTokens[0].toString(),
                    *prefixTokens.drop(1).map { it.toString() }.toTypedArray(),
                ).toString()
            }

            directories.addAll(prefixStrings)
        }
    }

    override fun delete(path: Path) {
        val pathStr = path.toString()
        synchronized(this) {
            directories.remove(pathStr)
            inMemoryMap.remove(pathStr)
        }
    }

    override fun copy(source: Path, target: Path, vararg options: CopyOption?) {
        val sourcePathStr = source.toString()
        val targetPathStr = target.toString()
        synchronized(this) {
            val newChildPaths = directories
                .filter { dir ->
                    dir.startsWith(sourcePathStr)
                }
                .map {
                    Paths.get(
                        targetPathStr,
                        it.drop(sourcePathStr.length),
                    ).toString()
                }
            directories.addAll(newChildPaths)

            val newFiles = inMemoryMap
                .filter { (k, _) ->
                    k.startsWith(sourcePathStr)
                }
                .map { (k, v) ->
                    Paths.get(
                        targetPathStr,
                        k.drop(sourcePathStr.length),
                    ).toString() to v
                }
            inMemoryMap.putAll(newFiles)
        }
    }

    override fun move(source: Path, target: Path, vararg options: CopyOption?) {
        val sourcePathStr = source.toString()
        val targetPathStr = target.toString()
        synchronized(this) {
            val childDirsToRemove = directories
                .filter { dir ->
                    dir.startsWith(sourcePathStr)
                }
                .toSet()
            val newChildPaths = childDirsToRemove
                .map {
                    Paths.get(
                        targetPathStr,
                        it.drop(sourcePathStr.length),
                    ).toString()
                }
            directories.removeAll(childDirsToRemove)
            directories.addAll(newChildPaths)

            val filesToRemove = inMemoryMap
                .filter { (k, _) ->
                    k.startsWith(sourcePathStr)
                }
            val newFiles = filesToRemove
                .map { (k, v) ->
                    Paths.get(
                        targetPathStr,
                        k.drop(sourcePathStr.length),
                    ).toString() to v
                }
            filesToRemove.forEach { (k, _) ->
                inMemoryMap.remove(k)
            }
            inMemoryMap.putAll(newFiles)
        }
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        return path.toString() == path2.toString()
    }

    override fun isHidden(path: Path): Boolean {
        return false
    }

    override fun getFileStore(path: Path): FileStore? {
        return null
    }

    override fun checkAccess(path: Path, vararg modes: AccessMode?) {
        //
    }

    override fun <V : FileAttributeView?> getFileAttributeView(
        path: Path,
        type: Class<V>?,
        vararg options: LinkOption?
    ): V? {
        return null
    }

    override fun <A : BasicFileAttributes?> readAttributes(
        path: Path,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A? {
        return null
    }

    override fun readAttributes(
        path: Path,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any>? {
        return null
    }

    override fun setAttribute(path: Path, attribute: String?, value: Any?, vararg options: LinkOption?) {
        //
    }

    companion object {

        private val innerInstance: MemoryMapFileSystemProvider by lazy {
            MemoryMapFileSystemProvider()
        }

        fun getInstance(): MemoryMapFileSystemProvider = innerInstance
        private const val FILE_SIZE_BYTES = 1024

    }
}