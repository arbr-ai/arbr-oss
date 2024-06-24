package com.arbr.data_common.base.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

fun interface DataRecordGroupFormatRecognizer<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {
    /**
     * Given metadata and contents of a record, decide its format.
     */
    fun recognizeFormat(
        recordGroupDescriptor: RecordGroupDescriptor<Obj, Grp>,
        completeUri: UriModel,
        resourceText: String,
    ): Mono<DataRecordObjectFormat>

    companion object {
        fun <
                Obj : DataRecordObject,
                Grp : RecordGrouping,
                > coalesce(
            formatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
            vararg formatRecognizers: DataRecordGroupFormatRecognizer<Obj, Grp>,
        ): DataRecordGroupFormatRecognizer<Obj, Grp> =
            DataRecordGroupFormatRecognizer { recordGroupDescriptor, completeUri, resourceText ->
                formatRecognizers.fold(
                    formatRecognizer.recognizeFormat(recordGroupDescriptor, completeUri, resourceText)
                ) { a, b ->
                    a.switchIfEmpty {
                        b.recognizeFormat(recordGroupDescriptor, completeUri, resourceText)
                    }
                }
            }
    }
}