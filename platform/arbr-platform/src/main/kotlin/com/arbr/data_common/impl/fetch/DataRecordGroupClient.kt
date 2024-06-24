package com.arbr.data_common.impl.fetch

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.format.DataRecordObjectParser
import com.arbr.data_common.base.format.DataVolumeObjectSourceScheme
import com.arbr.data_common.base.functional.DataRecordGroupDescriptorPaginator
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.data_common.base.functional.DataRecordObjectParsingConverter
import com.arbr.data_common.base.functional.DataRecordRetriever
import com.arbr.data_common.base.serialized.SerializedRecord
import com.arbr.data_common.impl.functional.DataRecordCollectionGroupInitializer
import com.arbr.data_common.impl.serialized.PlainStringSerializedRecord
import com.arbr.util_common.reactor.single
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface DataRecordGroupClient<
        Sch : DataVolumeObjectSourceScheme,
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > : DataRecordGroupDescriptorPaginator<Obj, Grp>, DataRecordRetriever<Obj, Grp> {

    val grouping: Grp
    val dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>
    val dataRecordConverter: DataRecordObjectParsingConverter<Obj>
    val recordGroupInitializer: DataRecordCollectionGroupInitializer<Obj, Grp>

    fun getRecordText(
        completeUri: UriModel,
    ): Mono<String>

    fun <Fmt : DataRecordObjectFormat> makeSerializedRecord(
        format: Fmt,
        resourceText: String,
    ): SerializedRecord<Fmt>

    private fun <Fmt : DataRecordObjectFormat> delimitSerializedRecordGroupText(
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

    override fun retrieveRecordGroup(
        recordGroupDescriptor: RecordGroupDescriptor<Obj, Grp>,
        completeUri: UriModel
    ): Mono<RecordGroup<Obj, Grp>> {
        return getRecordText(completeUri).flatMap { fileText ->
            dataRecordGroupFormatRecognizer.recognizeFormat(
                recordGroupDescriptor,
                completeUri,
                fileText,
            )
                .single("Unrecognized format for $completeUri")
                .flatMap { format ->
                    val dataRecordParser = DataRecordObjectParser.forFormat(format)

                    when (grouping) {
                        is RecordGrouping.Single -> {
                            val record = makeSerializedRecord(
                                format,
                                fileText,
                            )

                            record
                                .run(dataRecordParser::parse)
                                .run(dataRecordConverter::convertValue)
                                .map { objectRecord ->
                                    recordGroupInitializer.makeRecordGroup(objectRecord)
                                }
                        }

                        is RecordGrouping.Batch, RecordGrouping.BatchFixed -> {
                            delimitSerializedRecordGroupText(format, fileText)
                                .map(dataRecordParser::parse)
                                .concatMap(dataRecordConverter::convertValue)
                                .collectList()
                                .map { objectRecords ->
                                    recordGroupInitializer.makeRecordGroup(objectRecords)
                                }
                        }

                        else -> Mono.error(IllegalStateException())
                    }
                }
        }
    }


}

