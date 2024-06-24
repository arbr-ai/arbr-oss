package com.arbr.codegen.base.dependencies

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule

interface GenericObjectMapper {

    fun writeValueAsString(value: Any): String
    fun <T> convertValue(value: Any, clazz: Class<T>): T
    fun <T> convertValue(value: Any, typeRef: TypeReference<T>): T
}

inline fun <reified T> GenericObjectMapper.convertValue(value: Any): T = convertValue(value, T::class.java)

class MapperConfig {

    private fun asGeneric(
        objectMapper: ObjectMapper
    ): GenericObjectMapper {
        return object : GenericObjectMapper {
            override fun writeValueAsString(value: Any): String {
                return objectMapper.writeValueAsString(value)
            }

            override fun <T> convertValue(value: Any, clazz: Class<T>): T {
                return objectMapper.convertValue(value, clazz)
            }

            override fun <T> convertValue(value: Any, typeRef: TypeReference<T>): T {
                return objectMapper.convertValue(value, typeRef)
            }
        }
    }

    val yamlMapper = ObjectMapper(
        YAMLFactory()
            .disable(YAMLGenerator.Feature.SPLIT_LINES)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    ).also {
        val module = SimpleModule()
        it.registerModule(kotlinModule())
        // Add serializers + deserializers here...
        it.registerModule(module)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }.run(this::asGeneric)

    val mapper = jacksonObjectMapper().run(this::asGeneric)

}
