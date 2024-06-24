package com.arbr.data_common.impl.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.functional.DataRecordWriter
import com.arbr.data_common.impl.fs.MemoryMapPaths
import com.arbr.data_common.impl.serialized.PlainStringSerializedRecord
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

class MemoryMapRecordGroupWriter<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        Fmt : DataRecordObjectFormat,
        Ser : PlainStringSerializedRecord<Fmt>
        > : DataRecordWriter<Obj, Grp, Fmt, Ser> {

    private val outputMap = ConcurrentHashMap<UriModel, String>()

    override fun writeRecord(
        serializedRecordGroup: RecordGroup<Ser, Grp>,
        destinationUri: DataRecordFullyQualifiedUri
    ): Mono<Void> {
        return Mono.fromCallable {
            val uriModel = destinationUri.concat()
            val outPath = MemoryMapPaths.get(uriModel)

            // Assumes line-delimited groups
            val serializedText = serializedRecordGroup.flatten()
                .joinToString("\n") { it.getStringValue() }

            // TODO: Use the in-memory file system instead of a duplicate map
            synchronized(this) {
                outputMap[uriModel] = serializedText
            }

            Files.writeString(outPath, serializedText)
        }
            .subscribeOn(Schedulers.boundedElastic())
            .then()
    }

    fun getMap(): Map<UriModel, String> = synchronized(this) {
        outputMap.toMap()
    }
}
