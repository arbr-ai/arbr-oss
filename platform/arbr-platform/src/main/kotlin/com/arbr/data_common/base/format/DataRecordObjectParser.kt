package com.arbr.data_common.base.format

import com.arbr.content_formats.mapper.Mappers
import com.arbr.data_common.base.serialized.DataRecordMap
import com.arbr.data_common.base.serialized.SerializedRecord
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

fun interface DataRecordObjectParser<Fmt: DataRecordObjectFormat> {

    /**
     * Parse into an intermediate map form independent of the ultimate target object class:
     *
     * Should this allow a collection of maps for paged content without easy delimiters like zips?
     */
    fun parse(
        formattedValue: SerializedRecord<Fmt>,
    ): DataRecordMap

    data object FileJson: DataRecordObjectParser<DataRecordObjectFormat.FileJson> {
        private val mapper = Mappers.mapper

        override fun parse(formattedValue: SerializedRecord<DataRecordObjectFormat.FileJson>): DataRecordMap {
            return DataRecordMap(
                mapper.readValue(formattedValue.getStringValue(), jacksonTypeRef())
            )
        }
    }

    data object FileCsv: DataRecordObjectParser<DataRecordObjectFormat.FileCsv> {
        override fun parse(formattedValue: SerializedRecord<DataRecordObjectFormat.FileCsv>): DataRecordMap {
            throw NotImplementedError()
        }
    }

    data object FileYaml: DataRecordObjectParser<DataRecordObjectFormat.FileYaml> {
        private val yamlMapper = Mappers.yamlMapper

        override fun parse(formattedValue: SerializedRecord<DataRecordObjectFormat.FileYaml>): DataRecordMap {
            return DataRecordMap(
                yamlMapper.readValue(formattedValue.getStringValue(), jacksonTypeRef())
            )
        }
    }

    data object FileBinary: DataRecordObjectParser<DataRecordObjectFormat.FileBinary> {
        override fun parse(formattedValue: SerializedRecord<DataRecordObjectFormat.FileBinary>): DataRecordMap {
            throw NotImplementedError()
        }
    }

    data object FilePlaintext: DataRecordObjectParser<DataRecordObjectFormat.FilePlaintext> {
        override fun parse(formattedValue: SerializedRecord<DataRecordObjectFormat.FilePlaintext>): DataRecordMap {
            return DataRecordMap(
                mapOf(
                    "content" to formattedValue.getStringValue()
                )
            )
        }
    }

    data object FileByteBuffer: DataRecordObjectParser<DataRecordObjectFormat.FileByteBuffer> {
        override fun parse(formattedValue: SerializedRecord<DataRecordObjectFormat.FileByteBuffer>): DataRecordMap {
            throw NotImplementedError()
        }
    }

    data object DatabaseRecord: DataRecordObjectParser<DataRecordObjectFormat.DatabaseRecord> {
        override fun parse(formattedValue: SerializedRecord<DataRecordObjectFormat.DatabaseRecord>): DataRecordMap {
            throw NotImplementedError()
        }
    }

    companion object {
        fun <Fmt: DataRecordObjectFormat> forFormat(
            format: Fmt
        ): DataRecordObjectParser<Fmt> {
            @Suppress("UNCHECKED_CAST")
            return when (format) {
                DataRecordObjectFormat.FileJson -> FileJson
                DataRecordObjectFormat.FileCsv -> FileCsv
                DataRecordObjectFormat.FileYaml -> FileYaml
                DataRecordObjectFormat.FileBinary -> FileBinary
                DataRecordObjectFormat.FilePlaintext -> FilePlaintext
                DataRecordObjectFormat.FileByteBuffer -> FileByteBuffer
                DataRecordObjectFormat.DatabaseRecord -> DatabaseRecord
                else -> throw IllegalStateException()
            } as DataRecordObjectParser<Fmt>
        }
    }
}
