package com.arbr.content_formats.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class MapperConfig {

    @Bean
    @Primary
    fun mapper(): ObjectMapper = Mappers.mapper

    @Bean
    fun yamlMapper(): ObjectMapper = Mappers.yamlMapper

    @Bean("snakeCaseMapper")
    fun snakeCaseMapper() = Mappers.snakeCaseMapper

}
