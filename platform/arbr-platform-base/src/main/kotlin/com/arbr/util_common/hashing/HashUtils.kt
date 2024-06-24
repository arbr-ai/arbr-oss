package com.arbr.util_common.hashing

import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

object HashUtils {

    private const val BUF_SIZE_DEFAULT = 1024

    private fun hashByString(algorithm: String, content: String): String {
        val md = MessageDigest.getInstance(algorithm)

        val bytes = md.digest(content.toByteArray())
        val no = BigInteger(1, bytes)
        return no.toString(16)
    }

    fun sha1Hash(string: String): String {
        return hashByString("SHA-1", string)
    }

    fun sha1Hash(vararg strings: String): String {
        val md = MessageDigest.getInstance("SHA-1")

        strings.forEach {
            md.update(it.toByteArray())
        }
        val bytes = md.digest()
        val no = BigInteger(1, bytes)
        return no.toString(16)
    }

    fun sha256Hash(inputStream: InputStream, bufferSize: Int = BUF_SIZE_DEFAULT): String {
        val md = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(bufferSize)

        var len = inputStream.read(buffer)
        while (len != -1) {
            md.update(buffer, 0, len)
            len = inputStream.read(buffer)
        }

        val bytes = md.digest()
        val no = BigInteger(1, bytes)
        return no.toString(16)
    }

    fun sha256Hash(string: String): String {
        return hashByString("SHA-256", string)
    }

    fun sha224HashBytes(contentBytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-224")

        // This could be done more efficiently with buffers, but we should be working with small inputs. It would be
        // more valuable to constrain input size.
        val bytes = md.digest(contentBytes)
        val no = BigInteger(1, bytes)
        return no.toString(16)
    }

    /**
     * Hash a JSON-like object (used for AIApplications)
     */
    fun hashObject(md: MessageDigest, key: Any, obj: Any) {
        when (obj) {
            is Map<*, *> -> {
                val keys = obj.keys.sortedBy { it.toString() }
                for (k in keys) {
                    val value = obj[k]
                    if (value != null) {
                        hashObject(md, "$key.$k", value)
                    }
                }
            }

            is List<*> -> {
                obj.forEachIndexed { index, any ->
                    if (any != null) {
                        hashObject(md, "$key.$index", any)
                    }
                }
            }

            else -> {
                md.update("$key:$obj".toByteArray())
            }
        }
    }
}
