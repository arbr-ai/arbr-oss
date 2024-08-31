package com.arbr.platform.autoconfigure.process

import com.arbr.platform.object_graph.common.ObjectModelResourceParser
import com.arbr.platform.object_graph.core.ObjectModelParser
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnMissingBean(ObjectModelParser::class)
class ObjectModelParserAutoConfiguration(
    private val resourceParsers: List<ObjectModelResourceParser<*, *, *>>,
) {

    @Bean
    fun objectModelParser(): ObjectModelParser {
        return ObjectModelParser(resourceParsers)
    }
}
