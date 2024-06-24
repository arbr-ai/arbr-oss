package com.arbr.data_common.impl.functional

import com.arbr.content_formats.mapper.Mappers
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.functional.DataRecordObjectSerializingConverter
import com.arbr.data_common.base.serialized.DataRecordMap
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class DataRecordObjectSerializingJacksonConverterFactory(
    private val mapper: ObjectMapper,
) {

    fun <Obj : DataRecordObject> makeConverter(): DataRecordObjectSerializingConverter<Obj> {
        return DataRecordObjectSerializingConverter<Obj> { recordObject ->
            DataRecordMap(
                mapper.convertValue(recordObject, jacksonTypeRef<Map<String, Any?>>())
            )
        }
    }

    companion object {
        private val innerInstance by lazy {
            DataRecordObjectSerializingJacksonConverterFactory(
                Mappers.mapper
            )
        }

        fun getInstance(): DataRecordObjectSerializingJacksonConverterFactory = innerInstance
    }
}
