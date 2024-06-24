package com.arbr.prompt_library.transform

import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileSegmentOpDependency
import com.arbr.object_model.core.resource.field.*
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.prompt_library.util.FileSegmentOpDependencies
import com.arbr.prompt_library.util.FileSegmentOpDependencyEdges
import com.arbr.prompt_library.util.FileSegmentOpDependencyEdgesContainer
import com.arbr.prompt_library.util.FileSegmentOpDependencyEdgesValueRecord
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListParser
import com.arbr.content_formats.code.LenientCodeParser

object PropertyListParsers {

    val trailingContentPropertyListParser: PropertyListParser<SourcedStruct1<ArbrFileContentValue>> =
        object : PropertyListParser<SourcedStruct1<ArbrFileContentValue>> {
            override fun parseValue(
                propertySchemas: List<PropertySchema<*, *>>,
                messageContent: String,
                sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
                objectClass: Class<SourcedStruct1<ArbrFileContentValue>>
            ): SourcedStruct1<ArbrFileContentValue> {
                return SourcedStruct1(
                    ArbrFile.Content.generated(
                        LenientCodeParser.parse(messageContent.trim()),
                        sourcedValueGeneratorInfo,
                    )
                )
            }

        }

    class OutputParser(
        private val yamlMapper: ObjectMapper,
    ): PropertyListParser<FileSegmentOpDependencyEdgesValueRecord> {
        override fun parseValue(
            propertySchemas: List<PropertySchema<*, *>>,
            messageContent: String,
            sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
            objectClass: Class<FileSegmentOpDependencyEdgesValueRecord>
        ): FileSegmentOpDependencyEdgesValueRecord {
            val dependencyMap: Map<String, List<String>> = yamlMapper.readValue(
                LenientCodeParser.parse(messageContent),
                jacksonTypeRef(),
            )

            val dependencyEdges = FileSegmentOpDependencyEdges.initializeMerged(
                dependencyMap.entries.map { (opId, dependencyIds) ->
                    FileSegmentOpDependencyEdgesContainer(
                        ArbrFileSegmentOpDependency.DependencyFileSegmentOp.materialized(opId),
                        FileSegmentOpDependencies.initializeMerged(
                            dependencyIds.map { dependencyId ->
                                SourcedStruct1(
                                    ArbrFileSegmentOpDependency.DependencyFileSegmentOp.materialized(
                                        dependencyId
                                    ),
                                )
                            }
                        )
                    )
                }
            )

            return FileSegmentOpDependencyEdgesValueRecord(dependencyEdges)
        }
    }
}
