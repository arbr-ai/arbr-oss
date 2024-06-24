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
import java.net.URL

data class ClasspathResourceRecordGroupDescriptor<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    val dataVolumeUriComponent: DataVolumeUriComponent,
    val recordCollectionUriComponent: DataRecordCollectionUriComponent,

    /**
     * URL to the resource in the classpath - may be absolute.
     */
    val classpathResourceUrl: URL,
): RecordGroupDescriptor<Obj, Grp> {

    override fun getFullyQualifiedUri(
        dataVolume: DataVolume,
        dataRecordCollection: DataRecordCollection<Obj, Grp>
    ): DataRecordFullyQualifiedUri {
        // Stuff into volume as a URI
        return DataRecordFullyQualifiedUri(
            DataVolumeUriComponent(classpathResourceUrl.toURI()),
            DataRecordCollectionUriComponent(""),
            DataRecordUriComponent(""),
        )
    }
}


