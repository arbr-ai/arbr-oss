package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.platform.object_graph.common.values.collections.SourcedStruct
import com.arbr.platform.object_graph.common.values.SourcedValueGeneratorInfo
import com.arbr.relational_prompting.layers.object_translation.PropertySchema

interface PropertyListParser<R : SourcedStruct> {

    /**
     * Parse the message content, returning a parsed object.
     */
    fun parseValue(
        propertySchemas: List<PropertySchema<*, *>>,
        messageContent: String,
        sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
        objectClass: Class<R>,
    ): R?
}
