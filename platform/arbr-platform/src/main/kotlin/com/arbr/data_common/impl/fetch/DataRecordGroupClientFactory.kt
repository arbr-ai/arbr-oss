package com.arbr.data_common.impl.fetch

import com.arbr.aws.s3.S3Service
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataVolumeObjectSourceScheme
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.data_common.base.functional.DataRecordObjectParsingConverter
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.uri.DataVolumeUriScheme

/**
 * Implementations of client functionality under various parameter constraints.
 */
class DataRecordGroupClientFactory(
    private val s3Service: S3Service?,
) {

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > forFiles(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
        dataRecordConverter: DataRecordObjectParsingConverter<Obj>,
    ): DataRecordGroupClient<
            DataVolumeObjectSourceScheme.LocalFileSystem,
            Obj,
            Grp,
            > {
        return DataRecordGroupLocalFileSystemClient(
            dataVolume,
            recordCollection,
            dataRecordGroupFormatRecognizer,
            dataRecordConverter,
        )
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > forClasspath(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
        dataRecordConverter: DataRecordObjectParsingConverter<Obj>,
    ): DataRecordGroupClient<
            DataVolumeObjectSourceScheme.Classpath,
            Obj,
            Grp,
            > {
        return DataRecordGroupClasspathClient(
            dataVolume,
            recordCollection,
            dataRecordGroupFormatRecognizer,
            dataRecordConverter,
        )
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > forMemory(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
        dataRecordConverter: DataRecordObjectParsingConverter<Obj>,
    ): DataRecordGroupClient<
            DataVolumeObjectSourceScheme.Memory,
            Obj,
            Grp,
            > {
        return DataRecordGroupMemoryMapClient(
            dataVolume,
            recordCollection,
            dataRecordGroupFormatRecognizer,
            dataRecordConverter,
        )
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > forS3(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
        dataRecordConverter: DataRecordObjectParsingConverter<Obj>,
    ): DataRecordGroupClient<
            DataVolumeObjectSourceScheme.S3,
            Obj,
            Grp,
            >? {
        return if (s3Service == null) {
            null
        } else {
            DataRecordGroupS3Client(
                s3Service,
                dataVolume,
                recordCollection,
                dataRecordGroupFormatRecognizer,
                dataRecordConverter,
            )
        }
    }

    fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > makeClient(
        dataVolume: DataVolume,
        recordCollection: DataRecordCollection<Obj, Grp>,
        dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
        dataRecordConverter: DataRecordObjectParsingConverter<Obj>,
    ): DataRecordGroupClient<*, Obj, Grp>? {
        return when (dataVolume.scheme) {
            DataVolumeUriScheme.FILE -> forFiles(dataVolume, recordCollection, dataRecordGroupFormatRecognizer, dataRecordConverter)
            DataVolumeUriScheme.CLASSPATH -> forClasspath(dataVolume, recordCollection, dataRecordGroupFormatRecognizer, dataRecordConverter)
            DataVolumeUriScheme.MEMORY -> forMemory(dataVolume, recordCollection, dataRecordGroupFormatRecognizer, dataRecordConverter)
            DataVolumeUriScheme.S3 -> forS3(dataVolume, recordCollection, dataRecordGroupFormatRecognizer, dataRecordConverter)
            DataVolumeUriScheme.JDBC,
            DataVolumeUriScheme.R2DBC -> null
        }
    }

}
