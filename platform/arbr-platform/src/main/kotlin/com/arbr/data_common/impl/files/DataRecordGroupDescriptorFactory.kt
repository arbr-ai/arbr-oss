package com.arbr.data_common.impl.files

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.uri.DataVolumeUriScheme
import java.nio.file.Path

/**
 * Construct descriptors according to scheme.
 */
class DataRecordGroupDescriptorFactory {

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > forFiles(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        recordSubPath: Path,
    ): FileRecordGroupDescriptor<Obj, Grp> {
        return FileRecordGroupDescriptor(
            recordSubPath.toString(),
        )
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > forClasspath(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        recordSubPath: Path,
    ): ClasspathResourceRecordGroupDescriptor<Obj, Grp> {
        return ClasspathResourceRecordGroupDescriptor(
            dataVolume.uriComponent,
            recordCollection.uriComponent,
            recordSubPath.toUri().toURL(),
        )
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > forS3(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        recordSubPath: Path,
    ): S3ResourceRecordGroupDescriptor<Obj, Grp> {
        return S3ResourceRecordGroupDescriptor(
            dataVolume.uriComponent,
            recordCollection.uriComponent,
            recordSubPath.toString(),
        )
    }

    fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > makeDescriptor(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        recordSubPath: Path,
    ): RecordGroupDescriptor<Obj, Grp> {
        return when (dataVolume.scheme) {
            DataVolumeUriScheme.FILE -> forFiles(dataVolume, recordCollection, recordSubPath)
            DataVolumeUriScheme.CLASSPATH -> forClasspath(dataVolume, recordCollection, recordSubPath)
            DataVolumeUriScheme.MEMORY -> TODO()
            DataVolumeUriScheme.S3 -> forS3(dataVolume, recordCollection, recordSubPath)
            DataVolumeUriScheme.JDBC,
            DataVolumeUriScheme.R2DBC -> TODO()
        }
    }

}
