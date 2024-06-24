package com.arbr.engine.services.hasher

import com.arbr.util_common.hashing.HashUtils
import org.springframework.stereotype.Component

/**
 * The hash-slinging slasher
 */
@Component
class DefaultDocumentHasher: DocumentHasher {
    override fun hashContents(contentBytes: ByteArray): String {
        return HashUtils.sha224HashBytes(contentBytes)
    }
}
