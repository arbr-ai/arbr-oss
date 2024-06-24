package com.arbr.data_common.impl.fs

import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.*

class MemoryMapPath(fileSystem: MemoryMapFileSystem, path: ByteArray, normalized: Boolean) :
    Path {
    private val fileSystem: MemoryMapFileSystem
    private val path: ByteArray

    @Volatile
    private var offsets: IntArray = IntArray(0)

    @Volatile
    private var hash = 0

    @Volatile
    private var resolved: ByteArray = ByteArray(0)

    constructor(fileSystem: MemoryMapFileSystem, path: ByteArray) : this(fileSystem, path, false)

    init {
        this.fileSystem = fileSystem
        if (normalized) {
            this.path = path
        } else {
            this.path = normalize(path)
        }
    }

    override fun getFileSystem(): MemoryMapFileSystem {
        return fileSystem
    }

    override fun isAbsolute(): Boolean {
        return path.isNotEmpty() && path[0] == '/'.code.toByte()
    }

    override fun getRoot(): MemoryMapPath? {
        return if (isAbsolute) {
            MemoryMapPath(fileSystem, byteArrayOf(path[0]))
        } else null
    }

    override fun getFileName(): MemoryMapPath? {
        initOffsets()
        val nbOffsets = offsets.size
        if (nbOffsets == 0) {
            return null
        }
        if (nbOffsets == 1 && path[0] != '/'.code.toByte()) {
            return this
        }
        val offset = offsets[nbOffsets - 1]
        val length = path.size - offset
        val path = ByteArray(length)
        System.arraycopy(this.path, offset, path, 0, length)
        return MemoryMapPath(fileSystem, path)
    }

    override fun getParent(): MemoryMapPath? {
        initOffsets()
        val nbOffsets = offsets.size
        if (nbOffsets == 0) {
            return null
        }
        val length = offsets[nbOffsets - 1] - 1
        if (length <= 0) {
            return root
        }
        val path = ByteArray(length)
        System.arraycopy(this.path, 0, path, 0, length)
        return MemoryMapPath(fileSystem, path)
    }

    override fun getNameCount(): Int {
        initOffsets()
        return offsets.size
    }

    override fun getName(index: Int): MemoryMapPath {
        initOffsets()
        require(!(index < 0 || index >= offsets.size))
        val offset = offsets[index]
        val length: Int = if (index == offsets.size - 1) {
            path.size - offset
        } else {
            offsets[index + 1] - offset - 1
        }
        val path = ByteArray(length)
        System.arraycopy(this.path, offset, path, 0, length)
        return MemoryMapPath(fileSystem, path)
    }

    override fun subpath(beginIndex: Int, endIndex: Int): MemoryMapPath {
        initOffsets()
        require(!(beginIndex < 0 || beginIndex >= offsets.size || endIndex > offsets.size || beginIndex >= endIndex))
        val offset = offsets[beginIndex]
        val length: Int = if (endIndex == offsets.size) {
            path.size - offset
        } else {
            offsets[endIndex] - offset - 1
        }
        val path = ByteArray(length)
        System.arraycopy(this.path, offset, path, 0, length)
        return MemoryMapPath(fileSystem, path)
    }

    override fun startsWith(other: Path): Boolean {
        val p1 = this
        val p2 = checkPath(other)
        if (p1.isAbsolute != p2.isAbsolute || p1.path.size < p2.path.size) {
            return false
        }
        val length = p2.path.size
        for (idx in 0 until length) {
            if (p1.path[idx] != p2.path[idx]) {
                return false
            }
        }
        return p1.path.size == p2.path.size || p2.path[length - 1] == '/'.code.toByte() || p1.path[length] == '/'.code.toByte()
    }

    override fun startsWith(other: String): Boolean {
        return startsWith(getFileSystem().getPath(other))
    }

    override fun endsWith(other: Path): Boolean {
        val p1 = this
        val p2 = checkPath(other)
        var i1 = p1.path.size - 1
        if (i1 > 0 && p1.path[i1] == '/'.code.toByte()) {
            i1--
        }
        var i2 = p2.path.size - 1
        if (i2 > 0 && p2.path[i2] == '/'.code.toByte()) {
            i2--
        }
        if (i2 == -1) {
            return i1 == -1
        }
        if (p2.isAbsolute && (!isAbsolute || i2 != i1) || i1 < i2) {
            return false
        }
        while (i2 >= 0) {
            if (p2.path[i2] != p1.path[i1]) {
                return false
            }
            i2--
            i1--
        }
        return p2.path[i2 + 1] == '/'.code.toByte() || i1 == -1 || p1.path[i1] == '/'.code.toByte()
    }

    override fun endsWith(other: String): Boolean {
        return endsWith(getFileSystem().getPath(other))
    }

    override fun normalize(): MemoryMapPath {
        val p = getResolved()
        return if (p.contentEquals(path)) {
            this
        } else MemoryMapPath(fileSystem, p, true)
    }

    override fun resolve(other: Path): MemoryMapPath {
        val p1 = this
        val p2 = checkPath(other)
        if (p2.isAbsolute) {
            return p2
        }
        val result: ByteArray
        if (p1.path[p1.path.size - 1] == '/'.code.toByte()) {
            result = ByteArray(p1.path.size + p2.path.size)
            System.arraycopy(p1.path, 0, result, 0, p1.path.size)
            System.arraycopy(p2.path, 0, result, p1.path.size, p2.path.size)
        } else {
            result = ByteArray(p1.path.size + 1 + p2.path.size)
            System.arraycopy(p1.path, 0, result, 0, p1.path.size)
            result[p1.path.size] = '/'.code.toByte()
            System.arraycopy(p2.path, 0, result, p1.path.size + 1, p2.path.size)
        }
        return MemoryMapPath(fileSystem, result)
    }

    override fun resolve(other: String): MemoryMapPath {
        return resolve(getFileSystem().getPath(other))
    }

    override fun resolveSibling(other: Path): Path {
        val parent = parent
        return parent?.resolve(other) ?: other
    }

    override fun resolveSibling(other: String): Path {
        return resolveSibling(getFileSystem().getPath(other))
    }

    override fun relativize(other: Path): MemoryMapPath {
        val p1 = this
        val p2 = checkPath(other)
        if (p2 == p1) {
            return MemoryMapPath(fileSystem, ByteArray(0), true)
        }
        require(p1.isAbsolute == p2.isAbsolute)
        // Check how many segments are common
        val nbNames1 = p1.nameCount
        val nbNames2 = p2.nameCount
        val l = kotlin.math.min(nbNames1, nbNames2)
        var nbCommon = 0
        while (nbCommon < l && equalsNameAt(p1, p2, nbCommon)) {
            nbCommon++
        }
        var nbUp = nbNames1 - nbCommon
        // Compute the resulting length
        var length = nbUp * 3 - 1
        if (nbCommon < nbNames2) {
            length += p2.path.size - p2.offsets[nbCommon] + 1
        }
        // Compute result
        val result = ByteArray(length)
        var idx = 0
        while (nbUp-- > 0) {
            result[idx++] = '.'.code.toByte()
            result[idx++] = '.'.code.toByte()
            if (idx < length) {
                result[idx++] = '/'.code.toByte()
            }
        }
        // Copy remaining segments
        if (nbCommon < nbNames2) {
            System.arraycopy(p2.path, p2.offsets[nbCommon], result, idx, p2.path.size - p2.offsets[nbCommon])
        }
        return MemoryMapPath(fileSystem, result)
    }

    override fun toUri(): URI {
        return URI("memory", null, String(path), null, null)
    }

    override fun toAbsolutePath(): MemoryMapPath {
        if (isAbsolute) {
            return this
        }
        val result = ByteArray(path.size + 1)
        result[0] = '/'.code.toByte()
        System.arraycopy(path, 0, result, 1, path.size)
        return MemoryMapPath(fileSystem, result, true)
    }

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption?): MemoryMapPath {
        val absolute = MemoryMapPath(fileSystem, resolvedPath).toAbsolutePath()
        fileSystem.provider().checkAccess(absolute)
        return absolute
    }

    override fun toFile(): File {
        throw java.lang.UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun register(
        watcher: WatchService,
        events: Array<out WatchEvent.Kind<*>>?,
        vararg modifiers: WatchEvent.Modifier?
    ): WatchKey {
        throw java.lang.UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun register(watcher: WatchService, vararg events: WatchEvent.Kind<*>): WatchKey {
        throw java.lang.UnsupportedOperationException()
    }

    override fun iterator(): MutableIterator<Path> {
        return object : MutableIterator<Path> {
            private var index = 0
            override fun hasNext(): Boolean {
                return index < nameCount
            }

            override fun next(): Path {
                if (index < nameCount) {
                    val name = getName(index)
                    index++
                    return name
                }
                throw NoSuchElementException()
            }

            override fun remove() {
                throw java.lang.UnsupportedOperationException()
            }
        }
    }

    override fun compareTo(other: Path): Int {
        val p1 = this
        val p2 = checkPath(other)
        val a1 = p1.path
        val a2 = p2.path
        val l1 = a1.size
        val l2 = a2.size
        var i = 0
        val l = kotlin.math.min(l1, l2)
        while (i < l) {
            val b1 = a1[i].toInt() and 0xFF
            val b2 = a2[i].toInt() and 0xFF
            if (b1 != b2) {
                return b1 - b2
            }
            i++
        }
        return l1 - l2
    }

    private fun checkPath(paramPath: Path?): MemoryMapPath {
        if (paramPath == null) {
            throw NullPointerException()
        }
        if (paramPath !is MemoryMapPath) {
            throw ProviderMismatchException()
        }
        return paramPath
    }

    override fun hashCode(): Int {
        var h = hash
        if (h == 0) {
            hash = path.contentHashCode()
            h = hash
        }
        return h
    }

    override fun equals(other: Any?): Boolean {
        return other is MemoryMapPath && other.fileSystem === fileSystem && compareTo(other as MemoryMapPath?) == 0
    }

    override fun toString(): String {
        return String(path, StandardCharsets.UTF_8)
    }

    private fun initOffsets() {
        if (offsets.isEmpty()) {
            var count = 0
            var index = 0
            while (index < path.size) {
                val c = path[index++]
                if (c != '/'.code.toByte()) {
                    count++
                    while (index < path.size && path[index] != '/'.code.toByte()) {
                        index++
                    }
                }
            }
            val result = IntArray(count)
            count = 0
            index = 0
            while (index < path.size) {
                val m = path[index].toInt()
                if (m == '/'.code) {
                    index++
                } else {
                    result[count++] = index++
                    while (index < path.size && path[index] != '/'.code.toByte()) {
                        index++
                    }
                }
            }
            synchronized(this) {
                if (offsets.isEmpty()) {
                    offsets = result
                }
            }
        }
    }

    private val resolvedPath: ByteArray
        get() {
            var r = resolved
            if (r.isEmpty()) {
                resolved = if (isAbsolute) getResolved() else toAbsolutePath().resolvedPath
                r = resolved
            }
            return r
        }

    private fun normalize(path: ByteArray): ByteArray {
        if (path.isEmpty()) {
            return path
        }
        var i = 0
        for (j in path.indices) {
            val k = path[j].toInt()
            if (k == '\\'.code) {
                return normalize(path, j)
            }
            if (k == '/'.code && i == '/'.code) {
                return normalize(path, j - 1)
            }
            if (k == 0) {
                throw InvalidPathException(
                    String(path, StandardCharsets.UTF_8),
                    "Path: nul character not allowed"
                )
            }
            i = k
        }
        return path
    }

    private fun normalize(path: ByteArray, index: Int): ByteArray {
        val arrayOfByte = ByteArray(path.size)
        var i = 0
        while (i < index) {
            arrayOfByte[i] = path[i]
            i++
        }
        var j = i
        var k = 0
        while (i < path.size) {
            var m = path[i++].toInt()
            if (m == '\\'.code) {
                m = '/'.code
            }
            if (m != '/'.code || k != '/'.code) {
                if (m == 0) {
                    throw InvalidPathException(
                        String(path, StandardCharsets.UTF_8),
                        "Path: nul character not allowed"
                    )
                }
                arrayOfByte[j++] = m.toByte()
                k = m
            }
        }
        if (j > 1 && arrayOfByte[j - 1] == '/'.code.toByte()) {
            j--
        }
        return if (j == arrayOfByte.size) arrayOfByte else arrayOfByte.copyOf(j)
    }

    private fun getResolved(): ByteArray {
        if (path.isEmpty()) {
            return path
        }
        for (c in path) {
            if (c == '.'.code.toByte()) {
                return doGetResolved(this)
            }
        }
        return path
    }

    companion object {
        private fun doGetResolved(p: MemoryMapPath): ByteArray {
            val nc = p.nameCount
            val path = p.path
            val offsets = p.offsets
            val to = ByteArray(path.size)
            val lastM = IntArray(nc)
            var lastMOff = -1
            var m = 0
            for (i in 0 until nc) {
                var n = offsets[i]
                var len = if (i == offsets.size - 1) path.size - n else offsets[i + 1] - n - 1
                if (len == 1 && path[n] == '.'.code.toByte()) {
                    if (m == 0 && path[0] == '/'.code.toByte()) // absolute path
                        to[m++] = '/'.code.toByte()
                    continue
                }
                if (len == 2 && path[n] == '.'.code.toByte() && path[n + 1] == '.'.code.toByte()) {
                    if (lastMOff >= 0) {
                        m = lastM[lastMOff--] // retreat
                        continue
                    }
                    if (path[0] == '/'.code.toByte()) {  // "/../xyz" skip
                        if (m == 0) to[m++] = '/'.code.toByte()
                    } else {               // "../xyz" -> "../xyz"
                        if (m != 0 && to[m - 1] != '/'.code.toByte()) to[m++] = '/'.code.toByte()
                        while (len-- > 0) to[m++] = path[n++]
                    }
                    continue
                }
                if (m == 0 && path[0] == '/'.code.toByte() ||  // absolute path
                    m != 0 && to[m - 1] != '/'.code.toByte()
                ) {   // not the first name
                    to[m++] = '/'.code.toByte()
                }
                lastM[++lastMOff] = m
                while (len-- > 0) to[m++] = path[n++]
            }
            if (m > 1 && to[m - 1] == '/'.code.toByte()) m--
            return if (m == to.size) to else to.copyOf(m)
        }

        private fun equalsNameAt(p1: MemoryMapPath, p2: MemoryMapPath, index: Int): Boolean {
            val beg1 = p1.offsets[index]
            val len1: Int = if (index == p1.offsets.size - 1) {
                p1.path.size - beg1
            } else {
                p1.offsets[index + 1] - beg1 - 1
            }
            val beg2 = p2.offsets[index]
            val len2: Int = if (index == p2.offsets.size - 1) {
                p2.path.size - beg2
            } else {
                p2.offsets[index + 1] - beg2 - 1
            }
            if (len1 != len2) {
                return false
            }
            for (n in 0 until len1) {
                if (p1.path[beg1 + n] != p2.path[beg2 + n]) {
                    return false
                }
            }
            return true
        }
    }
}