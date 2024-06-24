package com.arbr.prompt_library.transform

import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.og.object_model.common.model.collections.NestedObjectListType2
import com.arbr.prompt_library.util.collateBy
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.layers.object_translation.TemplateDescriptionElement
import com.arbr.relational_prompting.layers.object_translation.TemplateValueElement
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListSerializer
import com.arbr.relational_prompting.layers.prompt_composition.YamlPropertyListSerializer

object TrailingContentPropertySerializers {

    val fileContentsPropertyListSerializer = TrailingContentPropertySerializer(
        listOf(
            TrailingContentPropertySerializer.SectionModel(
                "content",
                "File Contents:",
                { _ -> "```\n... file contents ...\n```" },
                { _, v -> v?.let { CodeSerializer.serializeCode(it as String) } ?: "" },
            )
        )
    )

    val errorContentsPropertySerializer = TrailingContentPropertySerializer(
        listOf(
            TrailingContentPropertySerializer.SectionModel(
                "error_content",
                "Error Contents:",
                { _ -> "```\n... error contents ...\n```" },
                { _, v -> v?.let { CodeSerializer.serializeCode(it as String) } ?: "" },
            )
        )
    )

    /**
     * TODO: Port
     */
    val trailingContentPropertyListSerializer = object : PropertyListSerializer {
        override fun describeProperties(propertySchemas: List<PropertySchema<*, *>>): TemplateDescriptionElement {
            val head = propertySchemas.dropLast(1)
            val headDescription = if (head.isEmpty()) {
                ""
            } else {
                YamlPropertyListSerializer.describeProperties(head).description
            }

            val tail = propertySchemas.takeLast(1)
            val tailDescription = if (tail.isEmpty()) {
                ""
            } else {
                "Current file contents:\n```\n... current file contents ...\n```"
            }

            return TemplateDescriptionElement(
                headDescription + "\n\n" + tailDescription
            )
        }

        override fun serializeValuedProperties(
            propertySchemas: List<PropertySchema<*, *>>,
            unwrappedPropertyValues: List<*>
        ): TemplateValueElement {
            val sections = propertySchemas.zip(unwrappedPropertyValues).collateBy {
                it.first.objectType == ArbrFile.Content
            }

            val textContent = sections.joinToString("\n\n") { (isFileContent, pairs) ->
                if (isFileContent) {
                    pairs.joinToString("\n") { (_, value) ->
                        val quotedContent = if (value == null) {
                            CodeSerializer.serializeCode("... empty ...")
                        } else {
                            // TODO: Language indicator
                            CodeSerializer.serializeCode(value.toString().trim())
                        }

                        "Current file contents:\n${quotedContent}"
                    }
                } else {
                    YamlPropertyListSerializer.serializeValuedProperties(
                        pairs.map { it.first },
                        pairs.map { it.second },
                    ).serializedValue
                }
            }

            return TemplateValueElement(
                textContent
            )
        }

    }

    fun fileSegmentTreePropertyListSerializer(
        yamlMapper: ObjectMapper,
    ): PropertyListSerializer {
        return object : PropertyListSerializer {
            override fun describeProperties(propertySchemas: List<PropertySchema<*, *>>): TemplateDescriptionElement {
                val treeDescription = """
                existing_source_element_tree:
                  element_rule_name: # Source Element Kind: The kind of source element, such as class, function, etc.
                  element_name: # Source Element Name: The name of the source element.
                  child_elements:
                    - element_rule_name: #
                      element_name: #
                      child_elements:
                        - element_rule_name: #
                          element_name: #
                          child_elements: [] # ...
                    - element_rule_name: #
                      element_name: #
                      child_elements: [] # ...
                    # ...
                new_single_source_element: # Info for single new source element to be incorporated
                  element_rule_name: # New Source Element Kind: The kind of the new source element, such as class, function, etc.
                  element_name: # New Source Element Name: The name of the new source element.
            """.trimIndent()

                return TemplateDescriptionElement(
                    CodeSerializer.serializeCode(
                        treeDescription,
                        "yaml"
                    )
                )
            }

            override fun serializeValuedProperties(
                propertySchemas: List<PropertySchema<*, *>>,
                unwrappedPropertyValues: List<*>
            ): TemplateValueElement {
                val templateTreeString = unwrappedPropertyValues.first() as String

                val treeMap: LinkedHashMap<String, Any> = yamlMapper.readValue(templateTreeString, jacksonTypeRef())

                val tailMap = linkedMapOf(
                    "element_rule_name" to unwrappedPropertyValues[1],
                    "element_name" to unwrappedPropertyValues[2],
                )
                treeMap["new_single_source_element"] = tailMap

                return TemplateValueElement(
                    CodeSerializer.serializeCode(
                        yamlMapper.writeValueAsString(treeMap),
                        "yaml"
                    )
                )
            }
        }
    }

    val commitCompletionPropertyListSerializer = object : PropertyListSerializer {
        private val numTrailingProperties = 2
        private val trailingProperties = listOf("relevant_other_file_contents", "updated_file_contents")

        override fun describeProperties(propertySchemas: List<PropertySchema<*, *>>): TemplateDescriptionElement {
            val head = propertySchemas.dropLast(numTrailingProperties)
            val headDescription = if (head.isEmpty()) {
                ""
            } else {
                YamlPropertyListSerializer.describeProperties(head).description
            }

            val tail = propertySchemas.takeLast(numTrailingProperties)
            val tailDescription = if (tail.size < numTrailingProperties) {
                ""
            } else {
                "Relevant other file contents:\n\n" +
                        "Relevant File 1 (file_name):\n" +
                        "```\n... file contents ...\n```\n\n" +
                        "Relevant File 2 (file_name):\n" +
                        "```\n... file contents ...\n```\n\n" +
                        "Updated file contents:\n\n" +
                        "Updated File 1 (file_name):\n" +
                        "```\n... file contents ...\n```\n\n" +
                        "Updated File 2 (file_name):\n" +
                        "```\n... file contents ...\n```\n\n..."
            }

            return TemplateDescriptionElement(
                headDescription + "\n\n" + tailDescription
            )
        }

        @Suppress("UNCHECKED_CAST")
        override fun serializeValuedProperties(
            propertySchemas: List<PropertySchema<*, *>>,
            unwrappedPropertyValues: List<*>
        ): TemplateValueElement {
            if (unwrappedPropertyValues.isEmpty()) {
                return TemplateValueElement("")
            }

            // Because of embeddings, don't assume all properties are here
            val collatedBuckets = propertySchemas
                .zip(unwrappedPropertyValues)
                .collateBy { (p, _) ->
                    val propertyKey = p.property().first
                    if (propertyKey in trailingProperties) propertyKey else "yaml"
                }

            val body = collatedBuckets.joinToString("\n\n") { (bucketKey: String, propValuePairs) ->
                val props = propValuePairs.map { it.first }
                val values = propValuePairs.map { it.second }

                if (bucketKey == "relevant_other_file_contents") {
                    val relevantOtherFileContents: List<NestedObjectListType2.InnerValue<String, String?>> = values.flatMap { elt ->
                        if (elt == null) {
                            emptyList()
                        } else {
                            elt as List<NestedObjectListType2.InnerValue<String, String?>>
                        }
                    }

                    if (relevantOtherFileContents.isEmpty()) {
                        ""
                    } else {
                        "Relevant other file contents:\n\n" + relevantOtherFileContents.withIndex()
                            .joinToString("\n\n") { (i, r) ->
                                val quotedContent = CodeSerializer.serializeCode(r.t2?.trim() ?: "").trim()

                                "Relevant File ${i + 1} (${r.t1}):\n$quotedContent"
                            }
                    }
                } else if (bucketKey == "updated_file_contents") {
                    val updatedFileContents: List<NestedObjectListType2.InnerValue<String, String?>> = values.flatMap { elt ->
                        if (elt == null) {
                            emptyList()
                        } else {
                            elt as List<NestedObjectListType2.InnerValue<String, String?>>
                        }
                    }

                    if (updatedFileContents.isEmpty()) {
                        ""
                    } else {
                        "Updated file contents:\n\n" + updatedFileContents.withIndex().joinToString("\n\n") { (i, r) ->
                            val quotedContent = CodeSerializer.serializeCode(r.t2?.trim() ?: "").trim()

                            "Updated ${i + 1} (${r.t1}):\n$quotedContent"
                        }
                    }
                } else {
                    YamlPropertyListSerializer.serializeValuedProperties(props, values).serializedValue
                }
            }

            return TemplateValueElement(
                body
            )
        }

    }

}
