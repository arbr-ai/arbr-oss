package com.arbr.data_common.impl.files

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.uri.DataRecordCollectionUriComponent
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import com.arbr.data_common.spec.uri.DataRecordUriComponent
import com.arbr.data_common.spec.uri.DataVolumeUriComponent
import java.nio.file.Paths

data class S3ResourceRecordGroupDescriptor<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    val dataVolumeUriComponent: DataVolumeUriComponent,
    val recordCollectionUriComponent: DataRecordCollectionUriComponent,

    /**
     * Key for the resource within the bucket.
     * Will include all of the path inside the bucket, overlapping with `recordCollectionUriComponent` and potentially
     * `dataVolumeUriComponent`.
     */
    val key: String,
): RecordGroupDescriptor<Obj, Grp> {

    override fun getFullyQualifiedUri(
        dataVolume: DataVolume,
        dataRecordCollection: DataRecordCollection<Obj, Grp>
    ): DataRecordFullyQualifiedUri {
        // Trim the key down to just the portion after the volume and record collection components.
        val keyPath = Paths.get(key)

        val containerFinalPathElement = Paths.get(
            dataVolumeUriComponent.uri
                .lenientExtendPath(recordCollectionUriComponent.uriComponent)
                .lenientEffectivePath
        ).last().toString()
        val finalElementIndex = keyPath.indexOfLast { it.toString() == containerFinalPathElement }

        val suffixPath = if (finalElementIndex == -1) {
            keyPath
        } else {
            val suffixPathTokens = keyPath.drop(finalElementIndex + 1).map { it.toString() }
            Paths.get(
                suffixPathTokens[0],
                *suffixPathTokens.drop(1).toTypedArray(),
            )
        }

        return DataRecordFullyQualifiedUri(
            dataVolumeUriComponent,
            recordCollectionUriComponent,
            DataRecordUriComponent(suffixPath.toString()),
        )
    }
}
