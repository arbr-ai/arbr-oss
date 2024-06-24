package com.arbr.data_common.impl.fetch

import com.arbr.aws.s3.S3Service
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.format.DataVolumeObjectSourceScheme
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.data_common.base.functional.DataRecordObjectParsingConverter
import com.arbr.data_common.base.serialized.SerializedRecord
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.impl.files.S3ResourceRecordGroupDescriptor
import com.arbr.data_common.impl.serialized.PlainStringSerializedRecord
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Paths

class DataRecordGroupS3Client<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    private val s3Service: S3Service,
    private val dataVolume: DataVolume,
    recordCollection: DataRecordCollection<Obj, Grp>,
    override val dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
    override val dataRecordConverter: DataRecordObjectParsingConverter<Obj>,

    ) : DefaultDataRecordGroupClient<
        DataVolumeObjectSourceScheme.S3,
        Obj,
        Grp,
        >(recordCollection) {

    /**
     * URI format: [s3://arbr-datasets/]  +  [com.arbr.alignable-diffs/noised/0/]  +  [n-101663032-1050679201-...]
     */
    private val recordCollectionPrefix = Paths.get(
        dataVolume.baseVolumePathString,
        recordCollection.uriComponent.uriComponent,
    )
        .toString()

    private fun listKeys(): Flux<String> {
        return s3Service.listObjectKeys(
            recordCollectionPrefix
        )
    }

    override fun getRecordText(completeUri: UriModel): Mono<String> {
        val key = completeUri.lenientEffectivePath
        return s3Service.getObjectText(key)
    }

    private fun <Fmt: DataRecordObjectFormat> delimitSerializedRecordGroupText(
        format: Fmt,
        resourceText: String,
    ): Flux<SerializedRecord<Fmt>> {
        // This is where we would delimit into serialized records inside a page
        val fileRecordTexts = listOf(resourceText)

        return Flux.fromIterable(fileRecordTexts)
            .map { recordText ->
                PlainStringSerializedRecord(format, recordText)
            }
    }

    override fun loadRecordGroupDescriptors(
        maxGroupSize: Int,
        maxNumGroupDescriptors: Int
    ): Flux<RecordGroupDescriptor<Obj, Grp>> {
        return listKeys()
            .map { key ->
                S3ResourceRecordGroupDescriptor(
                    dataVolume.uriComponent,
                    recordCollection.uriComponent,
                    key,
                )
            }
    }

}