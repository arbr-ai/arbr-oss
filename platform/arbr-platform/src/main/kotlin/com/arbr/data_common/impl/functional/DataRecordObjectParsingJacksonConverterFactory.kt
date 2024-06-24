package com.arbr.data_common.impl.functional

import com.arbr.content_formats.mapper.Mappers
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.functional.DataRecordObjectParsingConverter
import com.arbr.data_common.base.serialized.DataRecordMap
import com.fasterxml.jackson.databind.ObjectMapper
import reactor.core.publisher.Mono

class DataRecordObjectParsingJacksonConverterFactory(
    private val mapper: ObjectMapper,
) {

    fun <Obj : DataRecordObject> makeConverter(
        objClass: Class<Obj>,
    ): DataRecordObjectParsingConverter<Obj> {
        return object : DataRecordObjectParsingConverter<Obj> {
            override val targetObjectClass: Class<Obj> = objClass

            override fun convertValue(dataRecordMap: DataRecordMap): Mono<Obj> {
                return Mono.just(
                    mapper.convertValue(dataRecordMap.value, targetObjectClass)
                )
            }
        }
    }

    companion object {
        private val innerInstance by lazy {
            DataRecordObjectParsingJacksonConverterFactory(
                Mappers.mapper
            )
        }

        fun getInstance(): DataRecordObjectParsingJacksonConverterFactory = innerInstance
    }
}