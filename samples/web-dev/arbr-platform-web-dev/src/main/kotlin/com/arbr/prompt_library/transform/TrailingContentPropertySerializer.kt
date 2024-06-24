package com.arbr.prompt_library.transform

import com.arbr.prompt_library.util.collateBy
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.layers.object_translation.TemplateDescriptionElement
import com.arbr.relational_prompting.layers.object_translation.TemplateValueElement
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListSerializer
import com.arbr.relational_prompting.layers.prompt_composition.YamlPropertyListSerializer

class TrailingContentPropertySerializer(
    propertySectionModels: List<SectionModel>,
) : PropertyListSerializer {
    private val propertySectionMap: Map<String, SectionModel> = propertySectionModels.associateBy { it.key }

    data class SectionModel(
        val key: String,
        val sectionHeader: String?,
        val descriptionItemValue: (Int) -> String,
        val itemValue: (Int, Any?) -> String,
    )

    override fun describeProperties(propertySchemas: List<PropertySchema<*, *>>): TemplateDescriptionElement {
        // Because of embeddings, don't assume all properties are here
        val collatedBuckets = propertySchemas
            .collateBy { p ->
                val propertyKey = p.property().first
                if (propertySectionMap.containsKey(propertyKey)) propertyKey else "yaml"
            }

        val description = collatedBuckets.joinToString("\n\n") { (bucketKey: String, propList) ->
            val sectionModel = propertySectionMap[bucketKey]
            if (sectionModel == null) {
                YamlPropertyListSerializer.describeProperties(propList).description
            } else {
                val headerString = if (sectionModel.sectionHeader == null) {
                    ""
                } else {
                    sectionModel.sectionHeader + "\n"
                }

                headerString + propList.indices.joinToString("\n") { i ->
                    sectionModel.descriptionItemValue(i)
                }
            }
        }

        return TemplateDescriptionElement(
            description
        )
    }

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
                if (propertySectionMap.containsKey(propertyKey)) propertyKey else "yaml"
            }

        val body = collatedBuckets.joinToString("\n\n") { (bucketKey: String, propValuePairs) ->
            val propList = propValuePairs.map { it.first }
            val values = propValuePairs.map { it.second }

            val sectionModel = propertySectionMap[bucketKey]
            if (sectionModel == null) {
                YamlPropertyListSerializer.serializeValuedProperties(propList, values).serializedValue
            } else {
                val headerString = if (sectionModel.sectionHeader == null) {
                    ""
                } else {
                    sectionModel.sectionHeader + "\n"
                }

                headerString + values.withIndex().joinToString("\n") { (i, value) ->
                    sectionModel.itemValue(i, value)
                }
            }
        }

        return TemplateValueElement(
            body
        )
    }

}