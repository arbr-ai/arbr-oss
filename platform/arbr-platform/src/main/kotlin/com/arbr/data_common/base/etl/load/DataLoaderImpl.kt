package com.arbr.data_common.base.etl.load

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.format.DataRecordObjectSerializer
import com.arbr.data_common.base.functional.DataRecordObjectSerializingConverter
import com.arbr.data_common.base.functional.DataRecordWriter
import com.arbr.data_common.base.serialized.SerializedRecord
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import reactor.core.publisher.Flux

class DataLoaderImpl<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        Fmt : DataRecordObjectFormat,
        Ser : SerializedRecord<Fmt>
        >(
    override val outputVolume: DataVolume,
    override val outputRecordCollection: DataRecordCollection<Obj, Grp>,
    private val dataRecordObjectSerializingConverter: DataRecordObjectSerializingConverter<Obj>,
    private val dataRecordObjectSerializer: DataRecordObjectSerializer<Fmt, Ser>,
    private val dataRecordWriter: DataRecordWriter<Obj, Grp, Fmt, Ser>,
    private val dataLoaderConfigurationProperties: DataLoaderConfigurationProperties = DataLoaderConfigurationProperties(),
) : DataLoader<Obj, Grp> {

    private fun convertAndSerialize(
        output: Obj,
    ): Ser {
        return dataRecordObjectSerializingConverter.convertRecord(output)
            .run(dataRecordObjectSerializer::serializeRecord)
    }

    private fun convertAndSerializeGroup(
        recordGroup: RecordGroup<Obj, Grp>
    ): RecordGroup<Ser, Grp> {
        return recordGroup.map(this::convertAndSerialize)
    }

    /**
     * Load (write) elements and return URIs for each.
     */
    override fun loadToUris(
        outputElements: Flux<RecordGroupDescribedPair<Obj, Grp>>,
    ): Flux<DataRecordFullyQualifiedUri> {
        return outputElements
            .flatMap({ recordPageOutputElement ->
                val serializedGroup = convertAndSerializeGroup(recordPageOutputElement.recordGroup)
                val fqUri = recordPageOutputElement.recordGroupDescriptor
                    .getFullyQualifiedUri(outputVolume, outputRecordCollection)

                dataRecordWriter.writeRecord(
                    serializedGroup,
                    fqUri,
                )
                    .thenReturn(fqUri)
            }, dataLoaderConfigurationProperties.writeOutputParallelism)
    }

}