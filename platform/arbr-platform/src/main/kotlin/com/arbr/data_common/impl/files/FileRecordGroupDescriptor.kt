package com.arbr.data_common.impl.files

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import com.arbr.data_common.spec.uri.DataRecordUriComponent

data class FileRecordGroupDescriptor<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    val fileRecordRelativePathString: String,
): RecordGroupDescriptor<Obj, Grp> {

    override fun getFullyQualifiedUri(
        dataVolume: DataVolume,
        dataRecordCollection: DataRecordCollection<Obj, Grp>
    ): DataRecordFullyQualifiedUri {
        return DataRecordFullyQualifiedUri(
            dataVolume.uriComponent,
            dataRecordCollection.uriComponent,
            DataRecordUriComponent(fileRecordRelativePathString),
        )
    }
}
