package com.arbr.content_formats.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule

object Mappers {

    val mapper: ObjectMapper = jacksonObjectMapper().also {
        val module = SimpleModule()
        // Add serializers + deserializers here...
        it.registerModule(module)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    val yamlMapper: ObjectMapper = ObjectMapper(
        YAMLFactory()
            .disable(YAMLGenerator.Feature.SPLIT_LINES)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    ).also {
        val module = SimpleModule()
        it.registerModule(kotlinModule())
        // Add serializers + deserializers here...
        it.registerModule(module)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    /**
     * Mapper defaulting to snake case for reading and writing.
     */
    val snakeCaseMapper: ObjectMapper = mapper
        .setConfig(
            mapper
                .serializationConfig
                .with(PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE)
        )
        .setConfig(
            mapper
                .deserializationConfig
                .with(PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE)
        )

}
