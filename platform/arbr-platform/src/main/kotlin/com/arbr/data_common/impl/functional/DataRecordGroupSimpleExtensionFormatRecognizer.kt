package com.arbr.data_common.impl.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Mono

/**
 * Match URIs to formats based on simple known file extension cases.
 */
class DataRecordGroupSimpleExtensionFormatRecognizer<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >: DataRecordGroupFormatRecognizer<Obj, Grp> {

    /**
     * Given metadata and contents of a record, decide its format.
     */
    override fun recognizeFormat(
        recordGroupDescriptor: RecordGroupDescriptor<Obj, Grp>,
        completeUri: UriModel,
        resourceText: String,
    ): Mono<DataRecordObjectFormat> {
        val fileExtension = completeUri.lenientFileExtension()

        val format = when (fileExtension) {
            "json", "jsonl" -> DataRecordObjectFormat.FileJson
            "csv" -> DataRecordObjectFormat.FileCsv
            "yaml" -> DataRecordObjectFormat.FileYaml
            "txt" -> DataRecordObjectFormat.FilePlaintext
            else -> null  // Fall back to next recognizer
        }

        return Mono.justOrEmpty(format)
    }
}
