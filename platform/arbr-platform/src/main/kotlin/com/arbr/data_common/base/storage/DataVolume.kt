package com.arbr.data_common.base.storage

import com.arbr.data_common.spec.element.DataVolumeSpec
import com.arbr.data_common.spec.model.DataStorageMedium
import com.arbr.data_common.spec.uri.DataVolumeUriComponent
import com.arbr.data_common.spec.uri.DataVolumeUriScheme
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.toPath

interface DataVolume {

    /**
     * ID of the volume independent of which record collection and records it is used with.
     */
    val relativeId: String

    /**
     * Readable name for the volume.
     */
    val name: String

    /**
     * Component of the URI for the volume.
     */
    val uriComponent: DataVolumeUriComponent

    val scheme: DataVolumeUriScheme

    /**
     * Path component of the URI.
     */
    val baseVolumePathString: String

    /**
     * Priority for loading this volume relative to equivalent candidates, with precedence given to lower values.
     */
    val priority: Int

    val allowWrites: Boolean

    val storageMedium: DataStorageMedium

    companion object {
        private data class DataVolumeImpl(
            override val relativeId: String,
            override val name: String,
            override val uriComponent: DataVolumeUriComponent,
            override val scheme: DataVolumeUriScheme,
            override val baseVolumePathString: String,
            override val priority: Int,
            override val allowWrites: Boolean,
            override val storageMedium: DataStorageMedium,
        ) : DataVolume

        fun ofSpec(
            volumeSpec: DataVolumeSpec,
        ): DataVolume {
            val volumeUri = volumeSpec.uriComponent.uri

            val scheme = volumeUri.scheme.lowercase()
            val dataVolumeUriScheme = DataVolumeUriScheme.entries.firstOrNull {
                it.prefix == scheme
            } ?: throw IllegalArgumentException("Invalid scheme in URI $volumeUri")

            val volumePath = volumeUri.lenientEffectivePath

            return volumeSpec.run {
                DataVolumeImpl(
                    relativeId,
                    name,
                    uriComponent,
                    dataVolumeUriScheme,
                    volumePath,
                    priority,
                    allowWrites,
                    storageMedium,
                )
            }
        }
    }
}
