package com.arbr.data_common.impl.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Mono

/**
 * Yield a constant format.
 */
class DataRecordGroupConstantFormatRecognizer<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
            private val format: DataRecordObjectFormat,
        ): DataRecordGroupFormatRecognizer<Obj, Grp> {

    override fun recognizeFormat(
        recordGroupDescriptor: RecordGroupDescriptor<Obj, Grp>,
        completeUri: UriModel,
        resourceText: String,
    ): Mono<DataRecordObjectFormat> {
        return Mono.just(format)
    }
}