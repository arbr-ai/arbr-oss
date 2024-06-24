package com.arbr.data_common.spec.element

import com.arbr.data_common.spec.model.DataStorageMedium
import com.arbr.data_common.spec.uri.DataVolumeUriComponent

data class DataVolumeSpec(
    /**
     * ID of the volume independent of which record collection and records it is used with.
     */
    val relativeId: String,

    /**
     * Readable name for the volume.
     */
    val name: String,

    /**
     * Component of the URI for the volume.
     */
    val uriComponent: DataVolumeUriComponent,

    /**
     * Priority for loading this volume relative to equivalent candidates, with precedence given to lower values.
     */
    val priority: Int,

    val allowWrites: Boolean,
    val storageMedium: DataStorageMedium,
)
