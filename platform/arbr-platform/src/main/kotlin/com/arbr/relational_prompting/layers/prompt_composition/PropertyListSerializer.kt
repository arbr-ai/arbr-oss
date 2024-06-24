package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.layers.object_translation.TemplateDescriptionElement
import com.arbr.relational_prompting.layers.object_translation.TemplateValueElement


interface PropertyListSerializer {

    fun describeProperties(
        propertySchemas: List<PropertySchema<*, *>>,
    ): TemplateDescriptionElement

    /**
     * Serialize the values into prompt form.
     */
    fun serializeValuedProperties(
        propertySchemas: List<PropertySchema<*, *>>,
        unwrappedPropertyValues: List<*>,
    ): TemplateValueElement
}
