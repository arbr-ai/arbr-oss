package com.arbr.engine.services.hasher

sealed interface DocumentHasher {

    /**
     * Hash literal contents.
     */
    fun hashContents(contentBytes: ByteArray): String

}