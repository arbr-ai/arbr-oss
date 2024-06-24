package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.services.ai_application.config.OutputFormatException
import com.arbr.relational_prompting.services.ai_application.config.OutputSchemaExceptionWithKnownViolations
import com.arbr.content_formats.yaml.YamlParser
import com.arbr.content_formats.json_schematized.JsonSchemaValidation
import org.slf4j.LoggerFactory

class YamlPropertyListParser<R : SourcedStruct> : PropertyListParser<R> {
    private val logger = LoggerFactory.getLogger(YamlPropertyListParser::class.java)

    private val yamlParser = YamlParser()

    override fun parseValue(
        propertySchemas: List<PropertySchema<*, *>>,
        messageContent: String,
        sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
        objectClass: Class<R>,
    ): R {
        val possibleResultIterable = yamlParser.parseMap(
            propertySchemas,
            messageContent,
        )

        var latestException: Exception = Exception("Complete parsing failure")

        val possibleResultIterable1 = possibleResultIterable.toList()
        for ((parsedMap, parsingException) in possibleResultIterable1) {
            latestException = parsingException ?: latestException

            if (parsedMap != null) {
                try {
                    val prototypeRecordList = propertySchemas.map { propSchema ->
                        propSchema.objectType.parseFromObject(
                            SourcedValueKind.GENERATED,
                            parsedMap[propSchema.property().first],
                            sourcedValueGeneratorInfo
                        )
                    }

                    // If we can get a record out, return it immediately
                    @Suppress("UNCHECKED_CAST")
                    return SourcedStruct.of(prototypeRecordList) as R
                } catch (objectParsingException: Exception) {
                    // Attempt to extract violations
                    latestException = try {
                        val violations = JsonSchemaValidation.detectSchemaViolations(propertySchemas, parsedMap)

                        if (violations.isEmpty()) {
                            OutputFormatException(messageContent, objectParsingException)
                        } else {
                            // Throw a special error type so the consumer can decide whether/how to retry.
                            OutputSchemaExceptionWithKnownViolations(messageContent, parsedMap, violations, objectParsingException)
                        }
                    } catch (schemaViolationParsingException: Exception) {
                        OutputFormatException(messageContent, schemaViolationParsingException)
                    }
                }
            }
        }

        // No usable result
        throw latestException
    }

}