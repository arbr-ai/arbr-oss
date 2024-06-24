package com.arbr.data_common.base

import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import com.arbr.util_common.uri.UriModel

/**
 * A resource descriptor may provide coordinates for any number of individual resources, for example
 *  Manifest = text file with URLs of JSONL files
 *  Resource Descriptor = URL of JSONL file
 *  Resource = Model in one JSONL row
 */
interface RecordGroupDescriptor<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {

    fun getFullyQualifiedUri(
        dataVolume: DataVolume,
        dataRecordCollection: DataRecordCollection<Obj, Grp>,
    ): DataRecordFullyQualifiedUri

    fun getCompleteUri(
        dataVolume: DataVolume,
        dataRecordCollection: DataRecordCollection<Obj, Grp>,
    ): UriModel {
        val fqUri = getFullyQualifiedUri(dataVolume, dataRecordCollection)
        return fqUri.concat()
    }
}
