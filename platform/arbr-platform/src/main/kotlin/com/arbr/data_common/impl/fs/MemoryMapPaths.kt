package com.arbr.data_common.impl.fs

import com.arbr.data_common.spec.uri.DataVolumeUriScheme
import com.arbr.util_common.uri.UriModel
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

object MemoryMapPaths {

    fun get(uri: URI): MemoryMapPath {
        val uriModel = UriModel.ofUri(uri)
        return get(uriModel)
    }

    fun get(first: String, vararg more: String): MemoryMapPath {
        val plainPath = Path.of(first, *more)
        val uri = URI("memory", null, plainPath.toString(), null, null)
        return Paths.get(uri) as MemoryMapPath
    }

    fun get(uriModel: UriModel): MemoryMapPath {
        val memoryUri = when (uriModel) {
            is UriModel.SchemePath -> {
                URI(DataVolumeUriScheme.MEMORY.prefix, null, uriModel.path, null, null)
            }
            is UriModel.SchemeSsp -> {
                URI(DataVolumeUriScheme.MEMORY.prefix, uriModel.schemeSpecificPart, null)
            }
        }

        return Path.of(memoryUri) as MemoryMapPath
    }

}
