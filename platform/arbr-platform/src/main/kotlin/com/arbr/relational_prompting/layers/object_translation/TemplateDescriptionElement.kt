package com.arbr.relational_prompting.layers.object_translation

/**
 * A described element of a prompt template such as to be put in the instructions.
 */
data class TemplateDescriptionElement(
    /**
     * The rendered text describing the nature of the properties, like a comment. Usually independent of the actual
     * values.
     */
    val description: String,
)